/**
 * 列表加载混入
 * 提供上拉加载和下拉刷新的通用逻辑
 */
import { ref } from 'vue'
import { showError, showSuccess, hideLoading, showLoading } from '@/utils/helper'

export function useListLoad(fetchData, pageSize = 10) {
  const list = ref([])
  const loading = ref(false)
  const refreshing = ref(false)
  const noMore = ref(false)
  const currentPage = ref(1)

  // 加载数据
  const loadData = async (isRefresh = false) => {
    if (loading.value) return

    loading.value = true
    if (isRefresh) {
      currentPage.value = 1
      noMore.value = false
    }

    try {
      const result = await fetchData(currentPage.value, pageSize)

      if (isRefresh) {
        list.value = result.data || []
      } else {
        list.value = [...list.value, ...(result.data || [])]
      }

      // 判断是否还有更多数据
      if (result.data.length < pageSize) {
        noMore.value = true
      }

      return result
    } catch (error) {
      handleError(error)
      if (!isRefresh) {
        currentPage.value-- // 恢复页码
      }
    } finally {
      loading.value = false
      refreshing.value = false
    }
  }

  // 下拉刷新
  const onRefresh = async () => {
    refreshing.value = true
    await loadData(true)
  }

  // 上拉加载更多
  const onLoadMore = async () => {
    if (loading.value || noMore.value) return
    currentPage.value++
    await loadData(false)
  }

  return {
    list,
    loading,
    refreshing,
    noMore,
    currentPage,
    loadData,
    onRefresh,
    onLoadMore
  }
}

/**
 * 表单提交混入
 * 提供表单验证和提交的通用逻辑
 */
export function useFormSubmit(submitFn, validateFn) {
  const submitting = ref(false)

  const submit = async (...args) => {
    // 验证表单
    if (validateFn) {
      const valid = validateFn(...args)
      if (!valid) return
    }

    // 防止重复提交
    if (submitting.value) {
      showError('请勿重复提交')
      return
    }

    submitting.value = true
    showLoading('提交中...')

    try {
      const result = await submitFn(...args)
      hideLoading()

      showSuccess('提交成功')

      // 延迟返回上一页
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)

      return result
    } catch (error) {
      hideLoading()
      handleError(error, '提交失败')
      throw error
    } finally {
      submitting.value = false
    }
  }

  return {
    submitting,
    submit
  }
}

/**
 * 图片上传混入
 * 提供图片选择和上传的通用逻辑
 */
export function useImageUpload(maxCount = 9, maxSize = 5 * 1024 * 1024) {
  const imageList = ref([])
  const uploading = ref(false)

  // 选择图片
  const chooseImage = () => {
    const remainCount = maxCount - imageList.value.length
    if (remainCount <= 0) {
      showError(`最多只能上传${maxCount}张图片`)
      return
    }

    uni.chooseImage({
      count: remainCount,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePaths = res.tempFilePaths

        // 检查文件大小
        tempFilePaths.forEach(filePath => {
          uni.getFileInfo({
            filePath,
            success: (fileInfo) => {
              if (fileInfo.size > maxSize) {
                showError('图片大小不能超过5MB')
              }
            }
          })
        })

        uploadImages(tempFilePaths)
      }
    })
  }

  // 上传图片
  const uploadImages = async (filePaths) => {
    if (uploading.value) return

    uploading.value = true
    showLoading('上传中...')

    try {
      const uploadPromises = filePaths.map(filePath => {
        return new Promise((resolve, reject) => {
          uni.uploadFile({
            url: '/api/upload/image',
            filePath,
            name: 'file',
            header: {
              'Authorization': uni.getStorageSync('token')
            },
            success: (uploadFileRes) => {
              const data = JSON.parse(uploadFileRes.data)
              resolve({
                url: data.url,
                file: filePath
              })
            },
            fail: reject
          })
        })
      })

      const results = await Promise.all(uploadPromises)

      imageList.value.push(...results)

      hideLoading()
      showSuccess(`成功上传${results.length}张图片`)

      return results
    } catch (error) {
      hideLoading()
      handleError(error, '上传失败')
      throw error
    } finally {
      uploading.value = false
    }
  }

  // 删除图片
  const deleteImage = (index) => {
    uni.showModal({
      title: '提示',
      content: '确定要删除这张图片吗？',
      success: (res) => {
        if (res.confirm) {
          imageList.value.splice(index, 1)
        }
      }
    })
  }

  // 预览图片
  const previewImage = (index) => {
    const urls = imageList.value.map(item => item.url)
    uni.previewImage({
      current: index,
      urls
    })
  }

  return {
    imageList,
    uploading,
    chooseImage,
    deleteImage,
    previewImage
  }
}

/**
 * 分页混入
 * 提供分页数据的通用逻辑
 */
export function usePagination(fetchData, pageSize = 10) {
  const list = ref([])
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize_ref = ref(pageSize)
  const loading = ref(false)

  const loadData = async (page = currentPage.value) => {
    loading.value = true

    try {
      const result = await fetchData(page, pageSize_ref.value)

      list.value = result.data || []
      total.value = result.total || 0
      currentPage.value = page

      return result
    } catch (error) {
      handleError(error)
    } finally {
      loading.value = false
    }
  }

  // 改变页码
  const changePage = (page) => {
    loadData(page)
  }

  // 改变每页大小
  const changePageSize = (size) => {
    pageSize_ref.value = size
    loadData(1)
  }

  return {
    list,
    total,
    currentPage,
    pageSize: pageSize_ref,
    loading,
    loadData,
    changePage,
    changePageSize
  }
}
