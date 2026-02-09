<script setup>
import { ref, onMounted, computed } from "vue";
import {
	Edit,
	Delete,
	View,
	VideoCamera,
	Document,
	Plus,
	Star,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import api from "../../utils/api.js";
import { API_CONFIG } from "../../config/index.js";

// 数据
const tutorials = ref([]);
const loading = ref(false);
const showEditDialog = ref(false);
const dialogMode = ref("create");
const activeTab = ref("all"); // 当前激活的标签页

// 表单数据
const tutorialForm = ref({
	id: null,
	title: "",
	type: "article",
	content: "",
	difficulty: "BEGINNER",
	duration: "",
	calories: null,
	prep_time: "",
	servings: null,
	cover_image: "",
	featured: false,
	is_official: true,
	author: "官方", // 添加作者字段
});

// 统计数据
const stats = computed(() => {
	const all = tutorials.value;
	return {
		total: all.length,
		published: all.filter((t) => t.status === "PUBLISHED").length,
		pending: all.filter((t) => t.review_status === "PENDING").length,
		featured: all.filter((t) => t.featured).length,
		draft: all.filter((t) => t.status === "DRAFT").length,
		official: all.filter((t) => t.source_type === "ADMIN").length,
		merchant: all.filter((t) => t.source_type === "MERCHANT").length,
		user: all.filter((t) => t.source_type === "USER").length,
		ai: all.filter((t) => t.source_type === "AI_GENERATED").length,
	};
});

// 根据标签页过滤数据
const filteredTutorials = computed(() => {
	switch (activeTab.value) {
		case "all":
			return tutorials.value;
		case "published":
			return tutorials.value.filter((t) => t.status === "PUBLISHED");
		case "pending":
			return tutorials.value.filter((t) => t.review_status === "PENDING");
		case "draft":
			return tutorials.value.filter((t) => t.status === "DRAFT");
		default:
			return tutorials.value;
	}
});

// 获取各标签页的数量
const getTabCount = (tab) => {
	switch (tab) {
		case "all":
			return stats.value.total;
		case "published":
			return stats.value.published;
		case "pending":
			return stats.value.pending;
		case "draft":
			return stats.value.draft;
		default:
			return 0;
	}
};

// 获取所有教程
const fetchAllTutorials = async () => {
	loading.value = true;
	try {
		const response = await api.get(API_CONFIG.tutorial.adminList);
		console.log("管理员教程列表响应:", response);
		console.log("响应类型:", typeof response);
		console.log("是否为数组:", Array.isArray(response));

		// api拦截器已经返回了 response.data，所以 response 直接就是数据
		if (Array.isArray(response)) {
			tutorials.value = response;
			console.log(
				"✅ 数据已赋值给 tutorials.value，数组长度:",
				tutorials.value.length
			);
			console.log("✅ tutorials.value 前3条数据:", tutorials.value.slice(0, 3));
		} else if (response && response.data) {
			// 如果返回的是包装对象，取 data 字段
			tutorials.value = response.data;
			console.log(
				"✅ 数据已从 response.data 赋值，数组长度:",
				tutorials.value.length
			);
		} else {
			console.warn("⚠️ 响应数据格式不正确:", response);
		}
		console.log("📊 最终 tutorials.value:", tutorials.value);
	} catch (error) {
		console.error("获取教程列表失败:", error);
		console.error("错误详情:", error.response?.data);
		ElMessage.error("加载失败，请稍后重试");
	} finally {
		loading.value = false;
	}
};

// 打开创建对话框
const openCreateDialog = () => {
	dialogMode.value = "create";
	tutorialForm.value = {
		id: null,
		title: "",
		type: "article",
		content: "",
		difficulty: "BEGINNER",
		duration: "",
		calories: null,
		prep_time: "",
		servings: null,
		cover_image: "",
		featured: false,
		is_official: true,
		author: "官方",
	};
	showEditDialog.value = true;
};

// 打开编辑对话框
const openEditDialog = (tutorial) => {
	dialogMode.value = "edit";
	tutorialForm.value = {
		id: tutorial.id,
		title: tutorial.title,
		type: tutorial.type,
		content: tutorial.content,
		difficulty: tutorial.difficulty || "BEGINNER",
		duration: tutorial.duration || "",
		calories: tutorial.calories,
		prep_time: tutorial.prep_time,
		servings: tutorial.servings,
		cover_image: tutorial.cover_image || "",
		featured: tutorial.featured,
		is_official: tutorial.is_official,
	};
	showEditDialog.value = true;
};

// 保存教程
const saveTutorial = async () => {
	if (!tutorialForm.value.title) {
		ElMessage.warning("请输入教程标题");
		return;
	}
	if (!tutorialForm.value.content) {
		ElMessage.warning("请输入教程内容");
		return;
	}

	try {
		if (dialogMode.value === "create") {
			const response = await api.post(
				API_CONFIG.tutorial.adminCreate,
				tutorialForm.value
			);
			if (response?.id) {
				ElMessage.success("创建成功！");
				showEditDialog.value = false;
				fetchAllTutorials();
			}
		} else {
			// 更新教程
			const response = await api.put(
				`${API_CONFIG.tutorial.merchantUpdate}${tutorialForm.value.id}`,
				tutorialForm.value
			);
			if (response?.success) {
				ElMessage.success("更新成功！");
				showEditDialog.value = false;
				fetchAllTutorials();
			}
		}
	} catch (error) {
		console.error("保存失败:", error);
		ElMessage.error("保存失败，请稍后重试");
	}
};

// 删除教程
const deleteTutorial = async (tutorial) => {
	try {
		await ElMessageBox.confirm("删除后无法恢复，确认删除？", "删除教程", {
			confirmButtonText: "确认删除",
			cancelButtonText: "取消",
			type: "warning",
		});

		const response = await api.delete(
			`${API_CONFIG.tutorial.adminDelete}${tutorial.id}`
		);

		if (response?.success) {
			ElMessage.success("删除成功！");
			fetchAllTutorials();
		}
	} catch (error) {
		if (error !== "cancel") {
			console.error("删除失败:", error);
			ElMessage.error("删除失败，请稍后重试");
		}
	}
};

// 设置/取消精选
const toggleFeatured = async (tutorial) => {
	try {
		const response = await api.put(
			`${API_CONFIG.tutorial.adminToggleFeatured}${tutorial.id}/featured`,
			{ featured: !tutorial.featured }
		);

		if (response?.success) {
			ElMessage.success(tutorial.featured ? "已取消精选" : "已设为精选");
			fetchAllTutorials();
		}
	} catch (error) {
		console.error("操作失败:", error);
		ElMessage.error("操作失败，请稍后重试");
	}
};

// 获取来源类型标签
const getSourceTypeTag = (type) => {
	const map = {
		ADMIN: { type: "danger", text: "管理员" },
		MERCHANT: { type: "warning", text: "商家" },
		USER: { type: "success", text: "用户" },
		AI_GENERATED: { type: "info", text: "AI生成" },
	};
	return map[type] || { type: "info", text: type || "未知" };
};

// 获取状态标签
const getStatusTag = (status) => {
	const map = {
		DRAFT: { type: "info", text: "草稿" },
		PENDING: { type: "warning", text: "待审核" },
		PUBLISHED: { type: "success", text: "已发布" },
		REJECTED: { type: "danger", text: "已拒绝" },
	};
	return map[status] || { type: "info", text: status || "未知" };
};

// 获取审核状态标签
const getReviewStatusTag = (status) => {
	const map = {
		NOT_SUBMITTED: { type: "info", text: "未提交" },
		PENDING: { type: "warning", text: "待审核" },
		APPROVED: { type: "success", text: "已通过" },
		REJECTED: { type: "danger", text: "已拒绝" },
	};
	return map[status] || { type: "info", text: status || "未知" };
};

// 获取难度名称
const getDifficultyName = (difficulty) => {
	const map = {
		BEGINNER: "初级",
		INTERMEDIATE: "中级",
		ADVANCED: "高级",
	};
	return map[difficulty] || difficulty;
};

// 页面加载时获取数据
onMounted(() => {
	fetchAllTutorials();
});
</script>

<template>
	<div class="admin-tutorial-manage-container">
		<el-card shadow="never" class="tutorial-card">
			<template #header>
				<div class="header">
					<h3>教程管理</h3>
					<el-button type="primary" @click="openCreateDialog">
						<el-icon><Plus /></el-icon> 创建教程
					</el-button>
				</div>
			</template>

			<!-- 统计卡片 -->
			<div class="stats-cards">
				<div class="stat-card">
					<div class="stat-value">{{ stats.total }}</div>
					<div class="stat-label">总计</div>
				</div>
				<div class="stat-card published">
					<div class="stat-value">{{ stats.published }}</div>
					<div class="stat-label">已发布</div>
				</div>
				<div class="stat-card pending">
					<div class="stat-value">{{ stats.pending }}</div>
					<div class="stat-label">待审核</div>
				</div>
				<div class="stat-card featured">
					<div class="stat-value">{{ stats.featured }}</div>
					<div class="stat-label">精选</div>
				</div>
			</div>

			<!-- Tab切换 -->
			<el-tabs v-model="activeTab" style="margin-top: 20px" class="tutorial-tabs">
				<!-- 全部教程 -->
				<el-tab-pane name="all">
					<template #label>
						<span class="tab-label">
							全部教程
							<el-badge
								v-if="getTabCount('all') > 0"
								:value="getTabCount('all')"
								class="tab-badge"
							/>
						</span>
					</template>
					<el-table
						:data="filteredTutorials"
						v-loading="loading"
						stripe
						height="350"
					>
						<el-table-column prop="id" label="ID" width="80" />
						<el-table-column prop="title" label="教程标题" min-width="200" />

						<el-table-column label="来源" width="120">
							<template #default="{ row }">
								<el-tag
									:type="getSourceTypeTag(row.source_type).type"
									size="small"
								>
									{{ getSourceTypeTag(row.source_type).text }}
								</el-tag>
							</template>
						</el-table-column>

						<el-table-column label="类型" width="100">
							<template #default="{ row }">
								<el-icon
									v-if="row.type === 'video'"
									class="type-icon video"
								>
									<VideoCamera />
								</el-icon>
								<el-icon v-else class="type-icon article">
									<Document />
								</el-icon>
							</template>
						</el-table-column>

						<el-table-column label="状态" width="100">
							<template #default="{ row }">
								<el-tag
									:type="getStatusTag(row.status).type"
									size="small"
								>
									{{ getStatusTag(row.status).text }}
								</el-tag>
							</template>
						</el-table-column>

						<el-table-column label="审核状态" width="100">
							<template #default="{ row }">
								<el-tag
									:type="getReviewStatusTag(row.review_status).type"
									size="small"
								>
									{{ getReviewStatusTag(row.review_status).text }}
								</el-tag>
							</template>
						</el-table-column>

						<el-table-column label="精选" width="80">
							<template #default="{ row }">
								<el-tag v-if="row.featured" type="success" size="small">
									⭐
								</el-tag>
							</template>
						</el-table-column>

						<el-table-column label="难度" width="100">
							<template #default="{ row }">
								<el-tag v-if="row.difficulty" type="info" size="small">
									{{ getDifficultyName(row.difficulty) }}
								</el-tag>
							</template>
						</el-table-column>

						<el-table-column label="浏览量" width="120">
							<template #default="{ row }">
								<span>{{ row.view_count?.toLocaleString() || 0 }}</span>
							</template>
						</el-table-column>

						<el-table-column label="评分" width="100">
							<template #default="{ row }">
								<span v-if="row.rating">{{ row.rating }} ⭐</span>
								<span v-else style="color: #909399">-</span>
							</template>
						</el-table-column>

						<el-table-column
							label="操作"
							width="180"
							fixed="right"
							align="center"
						>
							<template #default="{ row }">
								<el-button-group>
									<el-button
										size="small"
										@click="openEditDialog(row)"
										title="编辑"
									>
										<el-icon><Edit /></el-icon>
									</el-button>
									<el-button
										:type="row.featured ? 'warning' : 'success'"
										size="small"
										@click="toggleFeatured(row)"
										:title="row.featured ? '取消精选' : '设为精选'"
									>
										<el-icon><Star /></el-icon>
									</el-button>
									<el-button
										type="danger"
										size="small"
										@click="deleteTutorial(row)"
										title="删除"
									>
										<el-icon><Delete /></el-icon>
									</el-button>
								</el-button-group>
							</template>
						</el-table-column>
					</el-table>
				</el-tab-pane>

				<!-- 已发布 -->
				<el-tab-pane name="published">
					<template #label>
						<span class="tab-label">
							已发布
							<el-badge
								v-if="getTabCount('published') > 0"
								:value="getTabCount('published')"
								class="tab-badge"
								type="success"
							/>
						</span>
					</template>
					<el-table
						:data="filteredTutorials"
						v-loading="loading"
						stripe
						height="350"
					>
						<el-table-column prop="id" label="ID" width="80" />
						<el-table-column prop="title" label="教程标题" min-width="200" />
						<el-table-column label="来源" width="120">
							<template #default="{ row }">
								<el-tag
									:type="getSourceTypeTag(row.source_type).type"
									size="small"
								>
									{{ getSourceTypeTag(row.source_type).text }}
								</el-tag>
							</template>
						</el-table-column>
						<el-table-column label="类型" width="100">
							<template #default="{ row }">
								<el-icon
									v-if="row.type === 'video'"
									class="type-icon video"
									><VideoCamera
								/></el-icon>
								<el-icon v-else class="type-icon article"
									><Document
								/></el-icon>
							</template>
						</el-table-column>
						<el-table-column label="难度" width="100">
							<template #default="{ row }">
								<el-tag v-if="row.difficulty" type="info" size="small">
									{{ getDifficultyName(row.difficulty) }}
								</el-tag>
							</template>
						</el-table-column>
						<el-table-column label="浏览量" width="120">
							<template #default="{ row }">
								<span>{{ row.view_count?.toLocaleString() || 0 }}</span>
							</template>
						</el-table-column>
						<el-table-column label="精选" width="80">
							<template #default="{ row }">
								<el-tag v-if="row.featured" type="success" size="small"
									>⭐</el-tag
								>
							</template>
						</el-table-column>
						<el-table-column
							label="操作"
							width="180"
							fixed="right"
							align="center"
						>
							<template #default="{ row }">
								<el-button-group>
									<el-button
										size="small"
										@click="openEditDialog(row)"
										title="编辑"
									>
										<el-icon><Edit /></el-icon>
									</el-button>
									<el-button
										:type="row.featured ? 'warning' : 'success'"
										size="small"
										@click="toggleFeatured(row)"
										:title="row.featured ? '取消精选' : '设为精选'"
									>
										<el-icon><Star /></el-icon>
									</el-button>
									<el-button
										type="danger"
										size="small"
										@click="deleteTutorial(row)"
										title="删除"
									>
										<el-icon><Delete /></el-icon>
									</el-button>
								</el-button-group>
							</template>
						</el-table-column>
					</el-table>
				</el-tab-pane>

				<!-- 待审核 -->
				<el-tab-pane name="pending">
					<template #label>
						<span class="tab-label">
							待审核
							<el-badge
								v-if="getTabCount('pending') > 0"
								:value="getTabCount('pending')"
								class="tab-badge"
								type="warning"
							/>
						</span>
					</template>
					<el-table
						:data="filteredTutorials"
						v-loading="loading"
						stripe
						height="350"
					>
						<el-table-column prop="id" label="ID" width="80" />
						<el-table-column prop="title" label="教程标题" min-width="200" />
						<el-table-column label="来源" width="120">
							<template #default="{ row }">
								<el-tag
									:type="getSourceTypeTag(row.source_type).type"
									size="small"
								>
									{{ getSourceTypeTag(row.source_type).text }}
								</el-tag>
							</template>
						</el-table-column>
						<el-table-column label="作者" width="120" prop="author" />
						<el-table-column label="类型" width="100">
							<template #default="{ row }">
								<el-icon
									v-if="row.type === 'video'"
									class="type-icon video"
									><VideoCamera
								/></el-icon>
								<el-icon v-else class="type-icon article"
									><Document
								/></el-icon>
							</template>
						</el-table-column>
						<el-table-column label="审核状态" width="100">
							<template #default="{ row }">
								<el-tag
									:type="getReviewStatusTag(row.review_status).type"
									size="small"
								>
									{{ getReviewStatusTag(row.review_status).text }}
								</el-tag>
							</template>
						</el-table-column>
						<el-table-column
							label="操作"
							width="140"
							fixed="right"
							align="center"
						>
							<template #default="{ row }">
								<el-button-group>
									<el-button
										type="primary"
										size="small"
										@click="openEditDialog(row)"
										title="审核"
									>
										<el-icon><Edit /></el-icon>
									</el-button>
									<el-button
										type="danger"
										size="small"
										@click="deleteTutorial(row)"
										title="删除"
									>
										<el-icon><Delete /></el-icon>
									</el-button>
								</el-button-group>
							</template>
						</el-table-column>
					</el-table>
				</el-tab-pane>

				<!-- 草稿 -->
				<el-tab-pane name="draft">
					<template #label>
						<span class="tab-label">
							草稿
							<el-badge
								v-if="getTabCount('draft') > 0"
								:value="getTabCount('draft')"
								class="tab-badge"
								type="info"
							/>
						</span>
					</template>
					<el-table
						:data="filteredTutorials"
						v-loading="loading"
						stripe
						height="350"
					>
						<el-table-column prop="id" label="ID" width="80" />
						<el-table-column prop="title" label="教程标题" min-width="200" />
						<el-table-column label="来源" width="120">
							<template #default="{ row }">
								<el-tag
									:type="getSourceTypeTag(row.source_type).type"
									size="small"
								>
									{{ getSourceTypeTag(row.source_type).text }}
								</el-tag>
							</template>
						</el-table-column>
						<el-table-column prop="author" label="作者" width="120" />
						<el-table-column
							prop="create_time"
							label="创建时间"
							width="180"
						/>
						<el-table-column
							label="操作"
							width="140"
							fixed="right"
							align="center"
						>
							<template #default="{ row }">
								<el-button-group>
									<el-button
										type="primary"
										size="small"
										@click="openEditDialog(row)"
										title="编辑"
									>
										<el-icon><Edit /></el-icon>
									</el-button>
									<el-button
										type="danger"
										size="small"
										@click="deleteTutorial(row)"
										title="删除"
									>
										<el-icon><Delete /></el-icon>
									</el-button>
								</el-button-group>
							</template>
						</el-table-column>
					</el-table>
				</el-tab-pane>
			</el-tabs>
		</el-card>

		<!-- 编辑对话框 -->
		<el-dialog
			v-model="showEditDialog"
			:title="dialogMode === 'create' ? '创建教程' : '编辑教程'"
			width="700px"
		>
			<el-form :model="tutorialForm" label-width="100px">
				<el-form-item label="教程标题" required>
					<el-input v-model="tutorialForm.title" placeholder="请输入教程标题" />
				</el-form-item>

				<el-form-item label="教程类型" required>
					<el-radio-group v-model="tutorialForm.type">
						<el-radio value="article">图文指南</el-radio>
						<el-radio value="video">视频教程</el-radio>
					</el-radio-group>
				</el-form-item>

				<el-form-item label="难度">
					<el-select v-model="tutorialForm.difficulty">
						<el-option label="初级" value="BEGINNER" />
						<el-option label="中级" value="INTERMEDIATE" />
						<el-option label="高级" value="ADVANCED" />
					</el-select>
				</el-form-item>

				<el-form-item label="时长">
					<el-input
						v-model="tutorialForm.duration"
						placeholder="例如: 15分钟"
					/>
				</el-form-item>

				<el-form-item label="卡路里">
					<el-input-number
						v-model="tutorialForm.calories"
						:min="0"
						:step="10"
						placeholder="千卡"
					/>
				</el-form-item>

				<el-form-item label="准备时间">
					<el-input
						v-model="tutorialForm.prep_time"
						placeholder="例如: 20分钟"
					/>
				</el-form-item>

				<el-form-item label="份量">
					<el-input-number
						v-model="tutorialForm.servings"
						:min="1"
						:max="20"
						placeholder="人份"
					/>
				</el-form-item>

				<el-form-item label="封面图URL">
					<el-input
						v-model="tutorialForm.cover_image"
						placeholder="图片URL（可选）"
					/>
				</el-form-item>

				<el-form-item label="设为精选">
					<el-switch v-model="tutorialForm.featured" />
				</el-form-item>

				<el-form-item label="官方认证">
					<el-switch v-model="tutorialForm.is_official" />
				</el-form-item>

				<el-form-item label="教程内容" required>
					<el-input
						v-model="tutorialForm.content"
						type="textarea"
						:rows="10"
						placeholder="支持Markdown格式，例如：## 标题、- 列表、**粗体**"
					/>
				</el-form-item>
			</el-form>

			<template #footer>
				<el-button @click="showEditDialog = false">取消</el-button>
				<el-button type="primary" @click="saveTutorial">
					{{ dialogMode === "create" ? "创建" : "保存" }}
				</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<style scoped lang="less">
.admin-tutorial-manage-container {
	padding: 20px;
	height: calc(100vh - 40px);
	overflow: hidden;
	display: flex;
	flex-direction: column;

	.tutorial-card {
		flex: 1;
		display: flex;
		flex-direction: column;
		overflow: hidden;

		:deep(.el-card__header) {
			flex-shrink: 0;
		}

		:deep(.el-card__body) {
			flex: 1;
			overflow: hidden;
			display: flex;
			flex-direction: column;
		}
	}

	.header {
		display: flex;
		justify-content: space-between;
		align-items: center;

		h3 {
			margin: 0;
			font-size: 20px;
			color: #303133;
		}
	}

	.stats-cards {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: 20px;
		margin-bottom: 16px;
		flex-shrink: 0;

		.stat-card {
			padding: 20px;
			border-radius: 8px;
			text-align: center;
			background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
			color: white;
			box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

			&.published {
				background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
			}

			&.pending {
				background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
			}

			&.featured {
				background: linear-gradient(135deg, #ffd700 0%, #ffb347 100%);
			}

			.stat-value {
				font-size: 32px;
				font-weight: bold;
				margin-bottom: 8px;
			}

			.stat-label {
				font-size: 14px;
				opacity: 0.9;
			}
		}
	}

	.type-icon {
		font-size: 18px;

		&.video {
			color: #ff6b6b;
		}

		&.article {
			color: #f7b267;
		}
	}

	// 教程标签页样式
	.tutorial-tabs {
		flex: 1;
		display: flex;
		flex-direction: column;
		overflow: hidden;

		:deep(.el-tabs__content) {
			flex: 1;
			overflow: hidden;
		}

		:deep(.el-tab-pane) {
			height: 100%;
		}

		:deep(.el-tabs__header) {
			margin-bottom: 16px;
			flex-shrink: 0;
		}

		:deep(.el-tabs__nav-wrap::after) {
			height: 1px;
		}

		:deep(.el-tabs__item) {
			font-size: 14px;
			padding: 0 20px;
			transition: all 0.3s;

			&:hover {
				color: #667eea;
			}

			&.is-active {
				color: #667eea;
				font-weight: 600;
			}
		}

		:deep(.el-tabs__active-bar) {
			background-color: #667eea;
			height: 3px;
			border-radius: 2px;
		}

		.tab-label {
			display: flex;
			align-items: center;
			gap: 8px;
			position: relative;

			.tab-badge {
				:deep(.el-badge__content) {
					font-size: 11px;
					height: 16px;
					line-height: 16px;
					padding: 0 5px;
					min-width: 18px;
					border-radius: 8px;
				}
			}
		}
	}

	// 表格样式优化
	:deep(.el-table) {
		border-radius: 8px;
		overflow: hidden;

		.el-table__header th {
			background-color: #fafafa;
			font-weight: 600;
			color: #606266;
		}

		.el-table__body tr:hover > td {
			background-color: #f5f7fa;
		}

		// 优化操作列的内边距
		.el-table__cell {
			padding: 12px 8px;

			// 操作列右侧单元格减少右边距
			&:last-child {
				padding-right: 12px;
			}
		}

		// 操作按钮之间的间距
		.el-button + .el-button {
			margin-left: 6px;
		}

		// 优化按钮组样式
		.el-button-group {
			.el-button {
				padding: 5px 8px;

				.el-icon {
					font-size: 14px;
				}
			}
		}

		// 表格滚动区域自定义滚动条
		.el-table__body-wrapper {
			// 滚动条覆盖显示
			&::-webkit-scrollbar {
				width: 8px;
				height: 8px;
			}

			&::-webkit-scrollbar-track {
				background: transparent;
			}

			&::-webkit-scrollbar-thumb {
				background: rgba(144, 147, 153, 0.3);
				border-radius: 4px;
				transition: background 0.3s;

				&:hover {
					background: rgba(144, 147, 153, 0.5);
				}
			}

			// Firefox 滚动条
			scrollbar-width: thin;
			scrollbar-color: rgba(144, 147, 153, 0.3) transparent;
		}
	}
}

@media (max-width: 1200px) {
	.stats-cards {
		grid-template-columns: repeat(2, 1fr);
	}
}

@media (max-width: 768px) {
	.stats-cards {
		grid-template-columns: 1fr;
	}
}
</style>
