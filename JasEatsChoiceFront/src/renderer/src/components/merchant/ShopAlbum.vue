<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ElImageViewer } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

const props = defineProps({
  merchantId: {
    type: String,
    required: true
  }
})

// 店铺相册
const shopAlbum = ref({
  environment: [],
  dishes: []
})

// 上传相关变量
const uploadAlbumType = ref('environment')
const imageUploadList = ref([])
const uploadSectionRef = ref(null) // 上传区域引用
const uploadInputRef = ref(null) // 上传输入框引用
const uploadComponentRef = ref(null) // 上传组件引用

// 图片预览相关
const showImageViewer = ref(false)
const previewImages = ref([])
const initialPreviewIndex = ref(0)

// 打开图片预览
const openImagePreview = (images, index) => {
  previewImages.value = images
  initialPreviewIndex.value = index
  showImageViewer.value = true
}

// 关闭图片预览
const closeImagePreview = () => {
  showImageViewer.value = false
}

// 获取店铺相册数据
const fetchMerchantAlbum = async () => {
  try {
    const response = await api.get(
      API_CONFIG.merchant.album.replace('{merchantId}', props.merchantId)
    )
    if (response.success && response.data) {
      console.log('相册原始数据:', response.data)

      // 后端返回的应该已经是完整的URL，直接使用
      // 如果返回的是相对路径，需要拼接
      const processImageUrls = (urls) => {
        if (!urls) return []
        return urls.map((url) => {
          console.log('处理图片URL:', url)
          // 如果URL已经是完整的HTTP地址，直接返回
          if (url.startsWith('http://') || url.startsWith('https://')) {
            return url
          }
          // 其他情况，返回原值（后端应该已经处理好）
          return url
        })
      }

      shopAlbum.value = {
        environment: processImageUrls(response.data.environment || []),
        dishes: processImageUrls(response.data.dishes || [])
      }

      console.log('处理后的相册数据:', shopAlbum.value)
    }
  } catch (error) {
    console.error('获取相册数据失败:', error)
  }
}

// 上传照片变更处理
const handleUpload = (file, fileList) => {
  // 处理文件列表,为每个文件添加预览URL
  imageUploadList.value = fileList.map((fileItem) => {
    // 如果文件还没有预览URL且有raw属性,创建本地预览URL
    if (!fileItem.url && fileItem.raw) {
      fileItem.url = URL.createObjectURL(fileItem.raw)
    }
    return fileItem
  })
}

// 移除上传的照片
const handleUploadRemove = (removedFile, fileList) => {
  console.log('移除上传的照片:', removedFile)
  // 释放Object URL避免内存泄漏
  if (removedFile.url && removedFile.url.startsWith('blob:')) {
    URL.revokeObjectURL(removedFile.url)
  }
  // 更新文件列表
  imageUploadList.value = fileList.map((fileItem) => {
    // 确保剩余文件的预览URL存在
    if (!fileItem.url && fileItem.raw) {
      fileItem.url = URL.createObjectURL(fileItem.raw)
    }
    return fileItem
  })
}

// 确认上传照片
const confirmUpload = () => {
  if (imageUploadList.value.length === 0) {
    ElMessage.warning('请先选择要上传的照片')
    return
  }

  const albumTypeText = '店铺环境'
  const formData = new FormData()

  // 添加照片文件到FormData（注意：后端期望的参数名是 'images'）
  imageUploadList.value.forEach((file) => {
    formData.append('images', file.raw)
  })

  // 添加相册类型
  formData.append('albumType', uploadAlbumType.value)

  console.log('开始上传照片...', {
    albumType: uploadAlbumType.value,
    fileCount: imageUploadList.value.length
  })

  // 调用后端API上传照片
  api
    .post(API_CONFIG.merchant.album.replace('{merchantId}', props.merchantId), formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    .then((response) => {
      console.log('上传响应完整数据:', response)
      console.log('上传响应success:', response?.success)
      console.log('上传响应data:', response?.data)

      // 兼容不同的响应格式 - 检查 response 本身是否成功
      const isSuccess = response?.success || response?.data?.success
      const responseData = response?.data !== undefined ? response.data : response

      if (isSuccess && responseData) {
        const uploadedImages = Array.isArray(responseData) ? responseData : []

        // 释放所有Object URL避免内存泄漏
        imageUploadList.value.forEach((file) => {
          if (file.url && file.url.startsWith('blob:')) {
            URL.revokeObjectURL(file.url)
          }
        })

        // 重新获取相册数据以确保一致性
        fetchMerchantAlbum()

        // 上传完成后清空上传列表
        imageUploadList.value = []

        // 显示上传成功提示
        ElMessage.success(`已成功追加上传${uploadedImages.length}张照片到${albumTypeText}相册`)
      } else {
        console.error('上传失败，响应格式不正确:', response)
        ElMessage.error('上传失败：' + (response?.message || '服务器返回错误'))
      }
    })
    .catch((error) => {
      console.error('上传照片失败:', error)
      ElMessage.error('上传照片失败：' + (error.message || '网络错误'))
    })
}

// 删除相册图片
const deleteAlbumImage = (type, index) => {
  const imageUrl = shopAlbum.value[type][index]

  // 确认删除
  ElMessageBox.confirm('确定要删除这张照片吗？', '删除照片', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 调用后端API删除照片
      api
        .delete(API_CONFIG.merchant.album.replace('{merchantId}', props.merchantId), {
          params: {
            imageUrl,
            albumType: type
          }
        })
        .then((response) => {
          // 修复响应判断逻辑
          if (response && response.success) {
            // 重新获取相册数据以确保一致性
            fetchMerchantAlbum()
            ElMessage.success('照片已删除')
          } else {
            ElMessage.error(response?.message || '删除失败')
          }
        })
        .catch((error) => {
          console.error('删除照片失败:', error)
          ElMessage.error('删除照片失败')
        })
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 触发立即上传（从空状态按钮）
const triggerUpload = () => {
  // 滚动到上传区域
  if (uploadSectionRef.value) {
    uploadSectionRef.value.scrollIntoView({
      behavior: 'smooth',
      block: 'center'
    })
  }
  // 触发文件选择对话框
  setTimeout(() => {
    const uploadInput = document.querySelector('.upload-area .el-upload__input')
    if (uploadInput) {
      uploadInput.click()
    }
  }, 500)
}

onMounted(() => {
  fetchMerchantAlbum()
})
</script>

<template>
  <div class="shop-album-card">
    <div class="album-header">
      <h4 class="card-title">📸 店铺环境 ({{ shopAlbum.environment.length }}张)</h4>
    </div>

    <!-- 店铺环境图片 -->
    <div class="album-section">
      <div v-if="shopAlbum.environment.length > 0" class="album-grid">
        <div
          v-for="(image, index) in shopAlbum.environment"
          :key="`env-${index}`"
          class="album-item"
          @click="openImagePreview(shopAlbum.environment, index)"
        >
          <div class="album-item-overlay">
            <el-button
              type="danger"
              size="small"
              circle
              @click.stop="deleteAlbumImage('environment', index)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-image :src="image" fit="contain">
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
      </div>

      <!-- 简化的空状态提示 -->
      <div v-if="shopAlbum.environment.length === 0" class="album-empty-simple">
        <el-icon class="empty-icon"><Picture /></el-icon>
        <p class="empty-text">暂无店铺环境图片</p>
        <el-button type="primary" size="small" @click="triggerUpload()">
          <el-icon><Plus /></el-icon> 立即上传
        </el-button>
      </div>
    </div>

    <!-- 上传图片 -->
    <div class="upload-section" ref="uploadSectionRef">
      <div class="upload-header">
        <h6 class="upload-title">
          <el-icon><Upload /></el-icon>
          上传店铺环境图片
        </h6>
      </div>

      <div class="upload-controls">
        <div class="upload-tips">
          <el-icon><InfoFilled /></el-icon>
          <span>支持 JPG/PNG 格式，单张不超过 5MB</span>
        </div>
      </div>

      <!-- 照片上传组件 -->
      <div class="upload-area">
        <el-upload
          ref="uploadComponentRef"
          action="#"
          :on-change="handleUpload"
          :on-remove="handleUploadRemove"
          :auto-upload="false"
          :file-list="imageUploadList"
          drag
          multiple
          :show-file-list="true"
          list-type="picture"
          accept="image/jpeg,image/jpg,image/png"
        >
          <el-icon class="el-icon-plus"><Plus /></el-icon>
          <div class="el-upload__text">
            <p class="upload-text">点击或拖拽文件到此处上传</p>
            <p class="upload-hint">支持多张图片同时上传</p>
          </div>
        </el-upload>

        <!-- 上传确认按钮 -->
        <div class="upload-actions">
          <el-button
            type="success"
            size="large"
            class="upload-confirm-btn"
            @click="confirmUpload"
            :disabled="imageUploadList.length === 0"
          >
            <el-icon><Select /></el-icon>
            确认上传
            {{ imageUploadList.length > 0 ? `(${imageUploadList.length}张)` : '' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 图片预览查看器 -->
    <el-image-viewer
      v-if="showImageViewer"
      :url-list="previewImages"
      :initial-index="initialPreviewIndex"
      @close="closeImagePreview"
    />
  </div>
</template>

<style scoped lang="less">
.shop-album-card {
  margin-bottom: 24px;
  padding: 24px;
  border: 2px solid #e6a23c; /* 橙色主题 */
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #fffbf5 100%);
  box-shadow: 0 4px 20px rgba(230, 162, 60, 0.15);

  .album-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 16px;

    .header-left {
      flex: 1;

      .card-title {
        margin: 0 0 12px 0;
        font-size: 1.429rem /* 原值: 20px */;
        font-weight: 700;
        color: #e6a23c;
      }

      .album-stats {
        display: flex;
        gap: 24px;
        font-size: 1rem /* 原值: 14px */;
        color: #606266;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 6px;
          padding: 4px 12px;
          background-color: #fff7e6;
          border-radius: 12px;
          transition: all 0.3s ease;

          &:hover {
            background-color: #ffe7ba;
            transform: translateY(-2px);
          }
        }
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
    }
  }

  .album-section {
    margin-bottom: 32px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .section-title {
        font-size: 1.143rem /* 原值: 16px */;
        font-weight: 600;
        color: #303133;
        margin: 0;
        display: flex;
        align-items: center;
        gap: 8px;
        padding-bottom: 8px;
        border-bottom: 2px solid #e6a23c;

        .title-icon {
          font-size: 1.429rem /* 原值: 20px */;
        }
      }
    }

    .album-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
      gap: 12px;
      margin-bottom: 12px;
    }

    .album-item {
      position: relative;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      transition: all 0.3s ease;
      cursor: pointer;
      background: #fff;
      padding: 8px;
      border: 1px solid #f0f0f0;

      &:hover {
        transform: translateY(-6px);
        box-shadow: 0 8px 24px rgba(230, 162, 60, 0.25);
      }

      // 添加预览提示层
      &::after {
        content: '🔍 点击预览';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(0, 0, 0, 0.5);
        color: #fff;
        font-size: 1rem /* 原值: 14px */;
        font-weight: 500;
        opacity: 0;
        transition: opacity 0.3s ease;
        z-index: 1;
        backdrop-filter: blur(2px);
      }

      &:hover::after {
        opacity: 1;
      }

      .album-item-checkbox {
        position: absolute;
        top: 8px;
        left: 8px;
        z-index: 2;
        background: rgba(255, 255, 255, 0.9);
        border-radius: 6px;
        padding: 4px;
        backdrop-filter: blur(4px);
      }

      .album-item-overlay {
        position: absolute;
        top: 8px;
        right: 8px;
        z-index: 2;
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      &:hover .album-item-overlay {
        opacity: 1;
      }

      .delete-img-btn {
        width: 36px;
        height: 36px;
        padding: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2px 12px rgba(245, 108, 108, 0.4);
        background: linear-gradient(135deg, #f56c6c, #ff8787);
        border: none;
        transition: all 0.3s ease;

        &:hover {
          transform: scale(1.1);
          box-shadow: 0 4px 16px rgba(245, 108, 108, 0.6);
        }
      }

      :deep(.el-image) {
        width: 100%;
        height: 120px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fff;
        overflow: hidden;
        border-radius: 8px;
        box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.05);

        .image-slot {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 100%;
          height: 100%;
          background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
          color: #909399;
          font-size: 2.286rem /* 原值: 32px */;
        }

        :deep(img) {
          max-width: 100%;
          max-height: 100%;
          width: auto;
          height: auto;
          object-fit: contain;
          padding: 4px;
        }
      }
    }
  }

  .album-empty-simple {
    text-align: center;
    padding: 40px 20px;
    color: #909399;

    .empty-icon {
      font-size: 3.429rem /* 原值: 48px */;
      margin-bottom: 12px;
      color: #e6a23c;
    }

    .empty-text {
      font-size: 1rem /* 原值: 14px */;
      margin-bottom: 16px;
    }
  }

  .upload-section {
    background: linear-gradient(135deg, #fffaf0 0%, #ffffff 100%);
    padding: 24px;
    border-radius: 12px;
    border: 2px solid #ffe7ba;
    margin-top: 24px;
    box-shadow: 0 2px 12px rgba(230, 162, 60, 0.08);

    .upload-header {
      margin-bottom: 20px;

      .upload-title {
        font-size: 1.143rem /* 原值: 16px */;
        font-weight: 600;
        color: #e6a23c;
        margin: 0;
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    .upload-controls {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      flex-wrap: wrap;
      gap: 16px;

      .upload-select {
        display: flex;
        align-items: center;
        gap: 12px;

        .upload-label {
          font-weight: 600;
          color: #303133;
          font-size: 1rem /* 原值: 14px */;
        }

        :deep(.el-select) {
          width: 200px;
        }
      }

      .upload-tips {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 0.929rem /* 原值: 13px */;
        color: #909399;
        padding: 6px 12px;
        background-color: #f5f7fa;
        border-radius: 6px;

        .el-icon {
          color: #409eff;
          font-size: 1rem /* 原值: 14px */;
        }
      }
    }

    .upload-area {
      display: flex;
      flex-direction: column;
      gap: 20px;

      :deep(.el-upload) {
        width: 100%;
      }

      :deep(.el-upload-dragger) {
        width: 100%;
        height: 200px;
        border: 2px dashed #d9a066;
        border-radius: 12px;
        background: linear-gradient(135deg, #fff9f0 0%, #ffffff 100%);
        transition: all 0.3s ease;

        &:hover {
          border-color: #e6a23c;
          background: linear-gradient(135deg, #ffe7ba 0%, #ffffff 100%);
        }

        .el-icon-plus {
          font-size: 3.429rem /* 原值: 48px */;
          color: #e6a23c;
          margin-bottom: 16px;
        }

        .el-upload__text {
          .upload-text {
            font-size: 1.143rem /* 原值: 16px */;
            color: #303133;
            font-weight: 500;
            margin-bottom: 8px;
          }

          .upload-hint {
            font-size: 0.929rem /* 原值: 13px */;
            color: #909399;
          }
        }
      }

      :deep(.el-upload-list) {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        margin-top: 16px;
        justify-content: center;

        .el-upload-list__item {
          width: 120px;
          height: 120px;
          border-radius: 8px;
          transition: box-shadow 0.3s ease;

          &:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          }

          .el-upload-list__item-name {
            display: none;
          }

          .el-upload-list__item-thumbnail {
            object-fit: contain;
            width: 100%;
            height: 100%;
            padding: 8px;
            box-sizing: border-box;
          }
        }
      }

      .upload-actions {
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 16px;
        border-radius: 12px;

        .upload-confirm-btn {
          min-width: 200px;
          font-size: 1.143rem /* 原值: 16px */;
          font-weight: 600;
          border: none;
          box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
          transition: all 0.3s ease;

          &:hover:not(:disabled) {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4);
          }

          &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
            background: #c0c4cc;
            box-shadow: none;
          }
        }
      }
    }
  }
}

// 自定义图片预览查看器样式
:deep(.el-image-viewer-wrapper) {
  .el-image-viewer__mask {
    background-color: #f5f5f5 !important;
  }

  .el-image-viewer__canvas {
    background-color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    box-shadow: 0 0 60px rgba(0, 0, 0, 0.1);

    img {
      background-color: #ffffff;
      object-fit: contain;
      max-width: 85vw;
      max-height: 85vh;
      width: auto !important;
      height: auto !important;
      padding: 20px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
      border-radius: 8px;
    }
  }

  .el-image-viewer__btn {
    background-color: rgba(0, 0, 0, 0.6);
    color: #fff;
    border-radius: 50%;
    width: 44px;
    height: 44px;

    &:hover {
      background-color: rgba(0, 0, 0, 0.8);
    }
  }

  .el-image-viewer__close {
    top: 40px;
    right: 40px;
  }
}
</style>
