<template>
	<div class="profile-container">
		<h2 class="page-title">个人中心</h2>

		<el-card class="profile-card">
			<!-- 顶部头像区域 -->
			<div class="profile-header">
				<!-- 单独拎出的名字 -->
				<div class="user-name-container">
					<h3 class="user-name">{{ userInfo.nickname || "未设置" }}</h3>
				</div>

				<div class="profile-content">
					<div class="avatar-wrapper">
						<CommonAvatar
							:avatar-url="avatarSrc"
							:fallback-text="userInfo.nickname || '未设置'"
							:size="120"
							:show-upload="true"
							:show-upload-button="false"
							:click-to-enlarge="true"
							ref="commonAvatarRef"
							@upload="handleAvatarUpload"
						/>
					</div>

					<div class="user-info-section">
						<div class="user-basic-info">
							<div class="user-stats">
								<div class="stat-row">
									<div class="stat-item">
										<span class="stat-label">手机号</span>
										<span class="stat-value">{{
											userInfo.phone || "未绑定"
										}}</span>
									</div>
									<div class="stat-item">
										<span class="stat-label">所在地</span>
										<span class="stat-value">{{
											userInfo.location || "未设置"
										}}</span>
									</div>
								</div>
								<div class="stat-row">
									<div class="stat-item">
										<span class="stat-label">今日摄入</span>
										<span class="stat-value calorie-highlight">{{
											userInfo.todayCalorie || "0kcal"
										}}</span>
									</div>
									<div class="stat-item">
										<span class="stat-label">本周均衡度</span>
										<span class="stat-value balance-highlight">{{
											userInfo.weekBalance || "0%"
										}}</span>
									</div>
								</div>
							</div>
						</div>

						<div class="action-buttons">
							<el-button
								type="primary"
								size="small"
								class="action-btn upload-avatar-btn"
								@click="triggerAvatarUpload"
								>📸 更换头像</el-button
							>
							<el-button
								type="primary"
								size="small"
								class="action-btn share-btn"
								@click="shareProfile"
								>📤 分享</el-button
							>
							<el-button
								type="primary"
								size="small"
								class="action-btn edit-btn"
								@click="editProfile"
								>✏️ 编辑资料</el-button
							>
						</div>
					</div>
				</div>
			</div>

			<el-divider />

			<div class="order-module">
				<h3 class="module-title">📜 订单模块</h3>
				<div class="order-stats">
					<div
						class="order-stat-card"
						@click="goToOrdersByStatus('processing')"
					>
						<div class="stat-value order-in-progress">
							{{ userInfo.orders?.inProgress || 0 }}笔
						</div>
						<div class="stat-label">进行中订单</div>
					</div>
					<div class="order-stat-card" @click="goToOrdersByStatus('pending')">
						<div class="stat-value order-pending">
							{{ userInfo.orders?.pending || 0 }}笔
						</div>
						<div class="stat-label">待确认订单</div>
					</div>
					<div
						class="order-stat-card"
						@click="goToOrdersByStatus('pendingComment')"
					>
						<div class="stat-value order-pending-comment">
							{{ userInfo.orders?.pendingComment || 0 }}笔
						</div>
						<div class="stat-label">待评价订单</div>
					</div>
				</div>
				<div style="display: flex; justify-content: flex-end; margin-top: 10px">
					<el-button type="primary" size="small" @click="goToAllOrders">
						<span>🔍 查看所有订单</span>
					</el-button>
				</div>
			</div>

			<el-divider />

			<div class="wallet-module">
				<h3 class="module-title">💰 钱包模块</h3>
				<div class="wallet-card">
					<div class="wallet-header">
						<div class="wallet-label">平台币余额</div>
					</div>
					<div class="wallet-balance">
						<span class="balance-number">{{
							userInfo.wallet?.balance || 0
						}}</span>
						<span class="balance-unit">个</span>
					</div>
					<div class="wallet-actions">
						<el-button
							type="primary"
							size="small"
							class="wallet-action-btn"
							@click="recharge"
						>
							💸 充值
						</el-button>
						<el-button
							type="primary"
							size="small"
							class="wallet-action-btn withdraw-btn"
							@click="withdraw"
						>
							📥 提现
						</el-button>
						<el-button type="text" size="small" @click="goToConsumeHistory">
							📊 消费记录
						</el-button>
					</div>
				</div>
			</div>

			<el-divider />

			<div class="other-modules">
				<div class="module-grid">
					<div class="module-item-card" @click="goToMyCollection">
						<div class="module-item-content">
							<div class="module-item-icon">🎁</div>
							<div class="module-item-info">
								<div class="module-item-title">我的收藏</div>
								<div class="module-item-desc">
									共{{ userInfo.collections || 0 }}个
								</div>
							</div>
						</div>
						<el-button
							type="text"
							size="small"
							class="module-item-btn"
							@click.stop="goToMyCollection"
						>
							查看收藏
						</el-button>
					</div>

					<div class="module-item-card" @click="goToAddress">
						<div class="module-item-content">
							<div class="module-item-icon">📝</div>
							<div class="module-item-info">
								<div class="module-item-title">我的地址</div>
								<div class="module-item-desc">
									共{{ userInfo.addresses || 0 }}个 | 默认地址：{{
										userInfo.defaultAddress || "未设置"
									}}
								</div>
							</div>
						</div>
						<el-button
							type="text"
							size="small"
							class="module-item-btn"
							@click.stop="goToAddress"
						>
							管理地址
						</el-button>
					</div>
				</div>
			</div>

			<el-divider />

			<div class="bottom-actions">
				<el-button type="text" size="small" @click="goToContact"
					>📞 联系客服</el-button
				>
				<el-button type="text" size="small" @click="submitFeedback"
					>🙋‍♂️ 反馈建议</el-button
				>
				<el-button type="text" size="small" danger @click="logout"
					>🔚 退出登录</el-button
				>
			</div>
		</el-card>

		<!-- 分享对话框 -->
		<el-dialog v-model="shareDialogVisible" title="分享个人中心" width="400px" center>
			<div class="share-content">
				<div class="share-link-section">
					<div class="section-title">分享链接</div>
					<el-input v-model="shareLink" readonly class="share-input" />
					<el-button
						type="primary"
						size="small"
						class="copy-btn"
						@click="copyShareLink"
					>
						📋 复制链接
					</el-button>
				</div>

				<div v-if="qrCodeDataUrl" class="qr-code-section">
					<div class="section-title">二维码分享</div>
					<img :src="qrCodeDataUrl" alt="分享二维码" class="qr-code" />
				</div>
			</div>

			<template #footer>
				<div class="dialog-footer">
					<el-button @click="shareDialogVisible = false">关闭</el-button>
				</div>
			</template>
		</el-dialog>

		<!-- 编辑资料对话框 -->
		<el-dialog
			v-model="editProfileDialogVisible"
			title="编辑资料"
			width="400px"
			center
		>
			<el-form
				ref="editFormRef"
				:model="editForm"
				:rules="editFormRules"
				label-width="80px"
				style="margin-top: 20px"
			>
				<el-form-item label="昵称" prop="nickname">
					<el-input v-model="editForm.nickname" placeholder="请输入昵称" />
				</el-form-item>

				<el-form-item label="手机号" prop="phone">
					<el-input
						v-model="editForm.phone"
						placeholder="请输入手机号"
						disabled
					/>
				</el-form-item>

				<el-form-item label="邮箱" prop="email">
					<el-input
						v-model="editForm.email"
						placeholder="请输入邮箱"
						type="email"
					/>
				</el-form-item>

				<el-form-item label="所在地" prop="location">
					<el-select
						v-model="selectedProvince"
						placeholder="请选择省份"
						style="width: 25%; margin-right: 10px"
						@change="handleProvinceChange"
					>
						<el-option
							v-for="province in provinces"
							:key="province.value"
							:label="province.label"
							:value="province.value"
						/>
					</el-select>
					<el-select
						v-model="selectedCity"
						placeholder="请选择城市"
						style="width: 25%; margin-right: 10px"
						@change="handleCityChange"
					>
						<el-option
							v-for="city in cities"
							:key="city.value"
							:label="city.label"
							:value="city.value"
						/>
					</el-select>
					<el-select
						v-model="selectedDistrict"
						placeholder="请选择区/县"
						style="width: 25%"
						@change="handleDistrictChange"
					>
						<el-option
							v-for="district in districts"
							:key="district.value"
							:label="district.label"
							:value="district.value"
						/>
					</el-select>
				</el-form-item>

				<!-- 身高输入框，允许输入小数点后一位 -->
				<el-form-item label="身高 (cm)" prop="height">
					<el-input
						v-model.number="editForm.height"
						placeholder="请输入身高"
						type="number"
						step="0.1"
					></el-input>
				</el-form-item>

				<!-- 体重输入框，允许输入小数点后一位 -->
				<el-form-item label="体重 (kg)" prop="weight">
					<el-input
						v-model.number="editForm.weight"
						placeholder="请输入体重"
						type="number"
						step="0.1"
					></el-input>
				</el-form-item>

				<el-form-item label="饮食目标" prop="dietGoal">
					<el-select v-model="editForm.dietGoal" placeholder="请选择饮食目标">
						<el-option label="减肥" value="减肥" />
						<el-option label="增肌" value="增肌" />
						<el-option label="保持健康" value="保持健康" />
					</el-select>
				</el-form-item>
			</el-form>

			<template #footer>
				<div class="dialog-footer">
					<el-button @click="editProfileDialogVisible = false">取消</el-button>
					<el-button type="primary" @click="saveEditProfile">保存</el-button>
				</div>
			</template>
		</el-dialog>
	</div>
</template>

<script setup>
// 导入依赖
import { ref, onMounted, computed, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import CommonAvatar from "../../components/CommonAvatar.vue";
import api from "../../utils/api";
import { API_CONFIG } from "../../config";
import QRCode from "qrcode";
import walletApi from "../../api/wallet";

// 导入状态管理
import { useAuthStore } from "../../store/authStore";
import { useUserStore } from "../../store/userStore";

// 初始化路由和状态管理
const router = useRouter();
const authStore = useAuthStore();
const userStore = useUserStore();

// 计算属性
// 头像来源 - 统一使用userStore中的头像信息
const avatarSrc = computed(() => {
	return userStore.userInfo?.avatar;
});

// 响应式变量 & Refs
// 用户信息
const userInfo = ref({
	name: "",
	phone: "",
	location: "",
	todayCalorie: "0kcal",
	weekBalance: "0%",
	orders: {
		inProgress: 0,
		pending: 0,
		pendingComment: 0,
	},
	wallet: {
		balance: 0,
	},
	collections: 0,
	addresses: 0,
	defaultAddress: "",
	avatar: "",
	height: 0,
	weight: 0,
});

// 组件引用
const commonAvatarRef = ref(null);

// 分享功能变量
const shareDialogVisible = ref(false);
const shareLink = ref("");
const qrCodeDataUrl = ref("");

// 资料编辑功能变量
const editProfileDialogVisible = ref(false);
const editForm = reactive({
	nickname: "",
	phone: "",
	email: "",
	location: "",
	height: 0,
	weight: 0,
	dietGoal: "",
});

// 地址选择功能变量
const selectedProvince = ref('');
const selectedCity = ref('');
const selectedDistrict = ref('');
const provinces = ref([]);
const cities = ref([]);
const districts = ref([]);
const cascaderData = ref([]);

// 资料编辑表单验证规则
const editFormRules = ref({
	nickname: [
		{ required: true, message: "请输入昵称", trigger: "blur" },
		{ min: 2, max: 20, message: "昵称长度在 2 到 20 个字符", trigger: "blur" },
	],
	email: [
		{ type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] },
	],
	location: [
		{ max: 50, message: "所在地长度不超过 50 个字符", trigger: ["blur", "change"] },
	],
// height 和 weight 的验证将在提交时手动处理
	height: [],
	weight: [],
	dietGoal: [{ required: true, message: "请选择饮食目标", trigger: "change" }],
});

// 生命周期钩子
// 页面加载时初始化
onMounted(async () => {
	// 从authStore获取userId
	let userId = parseInt(authStore.userId || "0", 10);

	console.log("userId:", userId);

	// 检查userId是否有效
	if (isNaN(userId) || userId <= 0) {
		ElMessage.error("用户未登录或登录信息无效，请重新登录");
		setTimeout(() => {
			router.push("/login");
		}, 1000);
		return;
	}

	// 如果当前用户信息为空或不完整，从后端API获取用户信息
	const isUserInfoEmpty =
		!userStore.userInfo ||
		Object.keys(userStore.userInfo).length === 0 ||
		!userStore.userInfo.nickname ||
		!userStore.userInfo.phone ||
		!userStore.userInfo.avatar ||
		!userStore.userInfo.avatar.length;

	if (isUserInfoEmpty) {
		console.log(
			"当前用户信息为空或不完整，从后端API获取用户信息",
			userStore.userInfo
		);
		// 从后端API获取用户信息
		userInfo.value = await userStore.fetchUserInfo(userId);
	} else {
		// 使用store中的用户信息
		userInfo.value = userStore.userInfo;
	}

	console.log("userInfo:", userInfo.value);
});

// 头像相关功能
// 触发头像上传
const triggerAvatarUpload = () => {
	// Trigger the hidden file input in the CommonAvatar component
	commonAvatarRef.value?.$refs?.avatarInput?.click();
};

// 处理头像上传
const handleAvatarUpload = (file) => {
	if (!file) return;

	const reader = new FileReader();
	reader.onload = async (e) => {
		// 获取完整的base64数据
		const base64Image = e.target.result;

		try {
			// 获取当前登录用户的ID
			const userId = authStore.userId;
			if (!userId) {
				ElMessage.error("用户未登录，请重新登录");
				return;
			}

			// 直接将base64图片上传到后端
			const response = await api.put(`/v1/users/${userId}/avatar/base64`, {
				avatarBase64: base64Image,
			});
			console.log("update avatar response:", response);
			if (response.code === "200") {
				console.log("update avatar success");
				userInfo.value = await userStore.fetchUserInfo(userId);

				ElMessage.success("头像上传成功");
			} else {
				ElMessage.error("头像上传失败: " + response.message);
			}
		} catch (error) {
			console.error("Avatar upload failed:", error);
			ElMessage.error("头像上传失败");
		}
	};

	reader.readAsDataURL(file);
};

// 导航功能
// 跳转到所有订单页面
const goToAllOrders = () => {
	router.push("/user/home/orders");
};

// 跳转到指定状态的订单
const goToOrdersByStatus = (status) => {
	router.push({
		path: "/user/home/orders",
		query: { status },
	});
};

// 跳转到消费记录页面
const goToConsumeHistory = () => {
	router.push("/user/home/consume-history");
};

// 跳转到我的收藏页面
const goToMyCollection = () => {
	router.push("/user/home/my-collection");
};

// 跳转到地址管理页面
const goToAddress = () => {
	router.push("/user/home/address");
};

// 钱包功能
// 充值功能
const recharge = async () => {
	const userId = parseInt(authStore.userId || "0", 10);
	if (userId <= 0) {
		ElMessage.error("用户未登录，请重新登录");
		return;
	}

	// 创建充值表单对话框
	ElMessageBox.prompt("请输入充值金额(单位:平台币)", "充值", {
		inputPattern: /^[1-9]\d*$/,
		inputValidator: (value) => {
			if (!value) {
				return "请输入充值金额";
			}
			if (Number(value) <= 0) {
				return "充值金额必须大于0";
			}
			return true;
		},
	})
		.then(async ({ value }) => {
			try {
				// 生成充值流水号
				const rechargeNo = "RCH" + new Date().getTime() + Math.floor(Math.random() * 1000);

				// 调用充值API
				const response = await walletApi.recharge(userId, Number(value), rechargeNo);

				if (response.code === "200") {
					// 更新本地余额
					userInfo.value.wallet.balance = response.data.balance || 0;

					ElMessage.success(`充值成功!已到账${value}平台币`);

					// 跳转到消费记录页面查看交易
					router.push("/user/home/consume-history");
				} else {
					ElMessage.error(response.message || "充值失败，请重试");
				}
			} catch (error) {
				console.error("充值失败:", error);
				ElMessage.error(error.message || "充值失败，请重试");
			}
		})
		.catch(() => {
			ElMessage.info("已取消充值");
		});
};

// 提现功能
const withdraw = async () => {
	const userId = parseInt(authStore.userId || "0", 10);
	if (userId <= 0) {
		ElMessage.error("用户未登录，请重新登录");
		return;
	}

	// 获取最新余额
	try {
		const balanceResponse = await walletApi.getBalance(userId);
		if (balanceResponse.code === "200") {
			userInfo.value.wallet.balance = balanceResponse.data || 0;
		}
	} catch (error) {
		console.error("获取余额失败:", error);
	}

	// 创建提现表单对话框
	ElMessageBox.prompt("请输入提现金额(单位:平台币)", "提现", {
		inputPattern: /^[1-9]\d*$/,
		inputValidator: (value) => {
			if (!value) {
				return "请输入提现金额";
			}
			const numValue = Number(value);
			const balance = Number(userInfo.value.wallet.balance);

			if (numValue <= 0) {
				return "提现金额必须大于0";
			}

			if (numValue > balance) {
				return `提现金额不能超过余额${balance}平台币`;
			}

			return true;
		},
	})
		.then(async ({ value }) => {
			try {
				// 生成提现流水号
				const withdrawNo = "WTH" + new Date().getTime() + Math.floor(Math.random() * 1000);

				// 调用提现API
				const response = await walletApi.withdraw(userId, Number(value), withdrawNo);

				if (response.code === "200") {
					// 更新本地余额
					userInfo.value.wallet.balance = response.data.balance || 0;

					ElMessage.success(`提现成功!已转出${value}平台币`);

					// 跳转到消费记录页面查看交易
					router.push("/user/home/consume-history");
				} else {
					ElMessage.error(response.message || "提现失败，请重试");
				}
			} catch (error) {
				console.error("提现失败:", error);
				ElMessage.error(error.message || "提现失败，请重试");
			}
		})
		.catch(() => {
			ElMessage.info("已取消提现");
		});
};

// 跳转到联系客服页面
const goToContact = () => {
	router.push("/user/home/contact");
};

// 设置功能
// 提交反馈建议
const submitFeedback = () => {
	ElMessage.success("反馈已提交，我们会尽快处理");
};

// 资料编辑功能
// 编辑资料
const editProfile = () => {
	// 处理 height 和 weight，确保它们不是数组类型
  console.log("userInfo:", userInfo.value);
	const userHeight = userInfo.value.height;
	const processedHeight = Array.isArray(userHeight)
		? Number(userHeight[0]) || 0
		: Number(userHeight) || 0;

	const userWeight = userInfo.value.weight;
	const processedWeight = Array.isArray(userWeight)
		? Number(userWeight[0]) || 0
		: Number(userWeight) || 0;

	// 将当前用户信息填充到编辑表单
	Object.assign(editForm, {
		nickname: userInfo.value.nickname || "",
		phone: userInfo.value.phone || "",
		email: userInfo.value.email || "",
		location: userInfo.value.location || "",
		// 确保 height 和 weight 始终为数字类型
		height: processedHeight,
		weight: processedWeight,
		dietGoal: userInfo.value.dietGoal || "",
	});

	// 初始化地址选择器
	initLocationSelect(userInfo.value.location || "");

	// 打开编辑资料对话框
	editProfileDialogVisible.value = true;
};

// 更新保存编辑的资料方法
const saveEditProfile = () => {
	if (editFormRef.value) {
    console.log("editForm:", editForm);
		editFormRef.value.validate(async (valid) => {
			if (valid) {
				// 手动验证身高和体重
				let isHeightValid = true;
				let isWeightValid = true;

				// 验证身高
				if (editForm.height !== null && editForm.height !== undefined && editForm.height !== '') {
					const heightNum = Number(editForm.height);
					if (isNaN(heightNum)) {
						isHeightValid = false;
						ElMessage.error("请输入有效的身高数值");
					} else if (heightNum < 30 || heightNum > 280) {
						isHeightValid = false;
						ElMessage.error("身高范围在 30 到 280 cm");
					}
				}

				// 验证体重
				if (editForm.weight !== null && editForm.weight !== undefined && editForm.weight !== '') {
					const weightNum = Number(editForm.weight);
					if (isNaN(weightNum)) {
						isWeightValid = false;
						ElMessage.error("请输入有效的体重数值");
					} else if (weightNum < 5 || weightNum > 300) {
						isWeightValid = false;
						ElMessage.error("体重范围在 5 到 300 kg");
					}
				}

				// 如果验证失败，返回
				if (!isHeightValid || !isWeightValid) {
					return;
				}

				// 再次确保提交前 height 和 weight 不是数组类型
				const submitForm = { ...editForm };
				if (Array.isArray(submitForm.height)) {
					submitForm.height = Number(submitForm.height[0]) || null;
				} else if (submitForm.height) {
					submitForm.height = Number(submitForm.height);
				}

				if (Array.isArray(submitForm.weight)) {
					submitForm.weight = Number(submitForm.weight[0]) || null;
				} else if (submitForm.weight) {
					submitForm.weight = Number(submitForm.weight);
				}
				try {
					const userId = parseInt(localStorage.getItem("userId"), 10);
					// 发送PUT请求更新用户资料
					const response = await api.put(
						API_CONFIG.user.update.replace("{userId}", userId),
						submitForm
					);

          console.log("更新用户信息响应:", response);
					if (response.code === "200") {
						// 更新本地用户信息
						const updatedUserInfo = { ...userInfo.value, ...editForm };
						userInfo.value = updatedUserInfo;
						// 更新store中的用户信息并保存到localStorage
						userStore.setUserInfo(updatedUserInfo);
						// 关闭对话框
						editProfileDialogVisible.value = false;
						ElMessage.success("资料更新成功");
					} else {
						ElMessage.error(
							"资料更新失败: " + (response.message || "未知错误")
						);
					}
				} catch (error) {
					console.error("更新资料失败:", error);
					ElMessage.error("网络请求失败，请稍后重试");
				}
			} else {
				ElMessage.error("表单验证失败，请检查输入");
			}
		});
	}
};

// 设置功能
// 退出登录
const logout = () => {
	// 弹出确认对话框
	ElMessageBox.confirm("确认要退出登录吗？", "提示", {
		confirmButtonText: "确定",
		cancelButtonText: "取消",
		type: "warning",
	})
		.then(() => {
			// 清除localStorage中的所有用户相关数据
			localStorage.removeItem("userInfo");
			localStorage.removeItem("userAvatar");
			localStorage.removeItem("phone");
			localStorage.removeItem("userId");
			localStorage.removeItem("token");
			// localStorage.removeItem('savedAccounts') // 也可以考虑清除保存的账号

			// 清除Store中的用户信息
			authStore.clearAuth();
			userStore.clearUserInfo();

			// 跳转到登录页面
			router.push("/login");
			ElMessage.success("已退出登录");
		})
		.catch(() => {
			// 取消退出登录
			ElMessage.info("已取消退出登录");
		});
};

// 地址选择功能

// 从后端获取地址数据
const fetchAddressData = async () => {
	try {
		const response = await api.get('/v1/location/cascader');
		console.log('获取地址数据成功:', response);
    if (response.code === '200' && response.data) {
			cascaderData.value = response.data;
			// 初始化省份列表
			provinces.value = cascaderData.value.map(province => ({
				label: province.label,
				value: province.value
			}));
		}
	} catch (error) {
		console.error('获取地址数据失败:', error);
		ElMessage.error('获取地址数据失败，请稍后重试');
	}
};

// 省份选择变化
const handleProvinceChange = () => {
	selectedCity.value = '';
	selectedDistrict.value = '';
	districts.value = [];
	if (selectedProvince.value) {
		// 从级联数据中找到对应的省份
		const currentProvince = cascaderData.value.find(province => province.value === selectedProvince.value);
		if (currentProvince && currentProvince.children) {
			cities.value = currentProvince.children.map(city => ({
				label: city.label,
				value: city.value
			}));
		} else {
			cities.value = [];
		}
	} else {
		cities.value = [];
	}
	updateLocation();
};

// 城市选择变化
const handleCityChange = () => {
	selectedDistrict.value = '';
	if (selectedProvince.value && selectedCity.value) {
		// 从级联数据中找到对应的省份和城市
		const currentProvince = cascaderData.value.find(province => province.value === selectedProvince.value);
		if (currentProvince && currentProvince.children) {
			const currentCity = currentProvince.children.find(city => city.value === selectedCity.value);
			if (currentCity && currentCity.children) {
				districts.value = currentCity.children.map(district => ({
					label: district.label,
					value: district.value
				}));
			} else {
				districts.value = [];
			}
		} else {
			districts.value = [];
		}
	} else {
		districts.value = [];
	}
	updateLocation();
};

// 区县选择变化
const handleDistrictChange = () => {
	updateLocation();
};

// 页面加载时获取地址数据
onMounted(async () => {
	await fetchAddressData();
});

// 更新完整地址到表单
const updateLocation = () => {
	const locationParts = [selectedProvince.value, selectedCity.value, selectedDistrict.value].filter(Boolean);
	editForm.location = locationParts.join(' ');
};

// 初始化地址选择器
const initLocationSelect = (location) => {
	if (!location) return;
	const parts = location.split(' ');
	if (parts.length >= 1) {
		selectedProvince.value = parts[0];
		handleProvinceChange();
		if (parts.length >= 2) {
			// 需要延迟一下，等待cities数据更新
			setTimeout(() => {
				selectedCity.value = parts[1];
				handleCityChange();
				if (parts.length >= 3) {
					setTimeout(() => {
						selectedDistrict.value = parts[2];
					}, 0);
				}
			}, 0);
		}
	}
};

// 编辑表单引用
const editFormRef = ref(null);

// 分享功能
const shareProfile = () => {
	// 生成分享链接
	const userId = parseInt(localStorage.getItem("userId") || "1", 10); // 模拟用户ID，实际应该从登录状态中获取
	shareLink.value = `${window.location.origin}/user/profile/${userId}`;

	// 生成二维码
	QRCode.toDataURL(shareLink.value, (err, url) => {
		if (err) {
			console.error("生成二维码失败:", err);
			qrCodeDataUrl.value = "";
		} else {
			qrCodeDataUrl.value = url;
		}
	});

	// 打开分享对话框
	shareDialogVisible.value = true;
};

// 复制分享链接
const copyShareLink = async () => {
	try {
		await navigator.clipboard.writeText(shareLink.value);
		ElMessage.success("分享链接已复制到剪贴板");
	} catch (err) {
		console.error("复制失败:", err);
		ElMessage.error("复制失败，请手动复制");
	}
};
</script>

<style scoped>
/* 基础容器样式 */
.profile-container {
	padding: 0 20px 20px 20px;
	min-height: 100vh;
}

.profile-card {
	padding: 25px;
	border-radius: 12px;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
	background-color: #fff;
}

/* 标题样式 */
.profile-container h2 {
	font-size: 28px;
	margin: 0 0 25px 0;
	color: #333;
	font-weight: 700;
}

.module-title {
	font-size: 18px;
	margin: 0 0 20px 0;
	font-weight: 700;
	color: #2d3748;
}

/* 顶部头像区域 */
.profile-header {
	display: flex;
	flex-direction: column; /* 改为纵向排列 */
	align-items: center; /* 居中对齐 */
	justify-content: center; /* 居中对齐 */
	gap: 25px; /* 元素间间距 */
	padding: 25px; /* 增加内边距 */
	flex-wrap: wrap;
}

.user-name-container {
	width: 100%;
	text-align: center; /* 名字居中 */
}

.user-name {
	font-size: 32px; /* 文字大小 */
	font-weight: 800; /* 字体粗细 */
	margin: 10px 0 20px 0; /* 上下间距 */
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); /* 文字渐变 */
	-webkit-background-clip: text; /* 将渐变应用到文字 */
	-webkit-text-fill-color: transparent; /* 文字填充为透明以显示渐变 */
	background-clip: text; /* 标准属性 */
	text-shadow: 2px 2px 4px rgba(102, 126, 234, 0.15); /* 文字阴影增强质感 */
	display: inline-block; /* 适应内容宽度 */
	letter-spacing: 1px; /* 字间距 */
	line-height: 1.2; /* 行高 */
}

/* 头像和用户信息内容区 */
.profile-content {
	display: flex;
	flex-direction: row;
	justify-content: space-evenly;
	align-items: center;
	gap: clamp(25px, 5vw, 45px); /* 响应式间距 */
	width: 100%;
	flex-wrap: wrap;
}

.avatar-wrapper {
	position: relative;
	flex-shrink: 0;
}

.user-info-section {
	min-width: 300px; /* 减小最小宽度，允许在更窄的屏幕上保持并排 */
	padding-right: 20px;
	display: flex;
	flex-direction: column;
	align-items: flex-end; /* 内容右对齐 */
}

.user-basic-info {
	margin-bottom: 20px;
}

/* 用户统计信息 */
.user-stats {
	font-size: 14px;
	margin-bottom: 20px;
}

.stat-row {
	display: flex;
	gap: clamp(25px, 4vw, 40px); /* 响应式间距 */
	margin-bottom: 12px;
	flex-wrap: wrap;
}

.stat-item {
	display: flex;
	flex-direction: column;
	gap: 4px;
	text-align: center;
}

.user-stats .stat-item {
	min-width: clamp(120px, 20vw, 140px); /* 响应式最小宽度 */
}

.stat-label {
	font-size: 14px;
	font-weight: 500;
	color: #718096;
}

.user-stats .stat-label {
	color: #606266;
	margin-bottom: 5px;
}

.stat-value {
	font-size: 18px;
	font-weight: 600;
	color: #2d3748;
}

.user-stats .stat-value {
	font-weight: bold;
	color: #ff6b6b;
}

.calorie-highlight {
	color: #ff6b6b;
}

.balance-highlight {
	color: #48bb78;
}

/* 操作按钮 */
.action-buttons {
	display: flex;
	justify-content: flex-end; /* 按钮右对齐 */
	gap: 12px;
	flex-wrap: wrap;
}

.action-btn {
	transition: all 0.2s ease;
}

.action-btn:hover {
	transform: translateY(-2px);
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.basic-info .info-item span {
	font-weight: bold;
}

/* 订单统计 */
.order-stats {
	display: flex;
	flex-wrap: wrap;
	gap: 20px;
	margin-bottom: 20px;
}

.order-stat-card {
	flex: 1;
	min-width: 140px;
	padding: 20px;
	background: linear-gradient(135deg, #f0f4f8 0%, #e2e8f0 100%);
	border-radius: 12px;
	text-align: center;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
	transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.order-stat-card:hover {
	transform: translateY(-2px);
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.order-stat-card .stat-value {
	font-size: 32px;
	color: #2d3748;
	margin-bottom: 6px;
}

.order-stat-card .stat-label {
	font-size: 14px;
	color: #718096;
	margin-bottom: 2px;
}

.order-stat-card small {
	font-size: 12px;
	color: #a0aec0;
}

.order-in-progress {
	color: #2b6cb0; /* 蓝色 */
}

.order-pending {
	color: #dd6b20; /* 橙色 */
}

.order-pending-comment {
	color: #805ad5; /* 紫色 */
}

/* 钱包模块 */
.wallet-card {
	background: linear-gradient(135deg, #fef5e7 0%, #fdebd0 100%);
	padding: 25px;
	border-radius: 12px;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.wallet-header {
	margin-bottom: 10px;
}

.wallet-label {
	font-size: 16px;
	color: #718096;
	font-weight: 500;
}

.wallet-balance {
	display: flex;
	align-items: baseline;
	gap: 8px;
	margin-bottom: 20px;
}

.balance-number {
	font-size: 48px;
	font-weight: 700;
	color: #d69e2e;
	font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

.balance-unit {
	font-size: 18px;
	color: #d69e2e;
	font-weight: 500;
}

.wallet-actions {
	display: flex;
	flex-wrap: wrap;
	gap: 12px;
	align-items: center;
}

.wallet-action-btn {
	background: linear-gradient(135deg, #f6e05e 0%, #ecc94b 100%);
	border: none;
	color: #2d3748;
	font-weight: 600;
	transition: transform 0.2s ease;
}

.wallet-action-btn:hover {
	transform: translateY(-2px);
	box-shadow: 0 4px 8px rgba(246, 224, 94, 0.4);
}

.withdraw-btn {
	background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
	color: #fff;
}

.withdraw-btn:hover {
	box-shadow: 0 4px 8px rgba(66, 153, 225, 0.4);
}

/* 其他模块 */
.other-modules {
	margin-bottom: 20px;
}

.module-grid {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
	gap: 20px;
}

.module-item-card {
	background: linear-gradient(135deg, #ebf8ff 0%, #bee3f8 100%);
	padding: 20px;
	border-radius: 12px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
	display: flex;
	justify-content: space-between;
	align-items: center;
	transition: all 0.3s ease;
	cursor: pointer;
}

.module-item-card:hover {
	transform: translateY(-2px);
	box-shadow: 0 4px 12px rgba(190, 227, 248, 0.3);
}

.module-item-content {
	display: flex;
	align-items: center;
	gap: 12px;
}

.module-item-icon {
	font-size: 24px;
	line-height: 1;
}

.module-item-info {
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.module-item-title {
	font-size: 16px;
	font-weight: 600;
	color: #2b6cb0;
}

.module-item-desc {
	font-size: 14px;
	color: #718096;
}

.module-item-btn {
	color: #2b6cb0;
	font-weight: 600;
}

/* 底部操作按钮 */
.bottom-actions {
	margin-top: 30px;
	display: flex;
	flex-wrap: wrap;
	gap: 15px;
}

.bottom-actions .el-button {
	flex: 1;
	min-width: 120px;
	height: 40px;
	border-radius: 8px;
	font-weight: 600;
	transition: transform 0.2s ease;
}

.bottom-actions .el-button:hover {
	transform: translateY(-2px);
}

.bottom-actions .el-button:nth-child(1) {
	background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
	border: none;
	color: #fff;
}

.bottom-actions .el-button:nth-child(2) {
	background: linear-gradient(135deg, #9f7aea 0%, #805ad5 100%);
	border: none;
	color: #fff;
}

.bottom-actions .el-button:nth-child(3) {
	background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
	border: none;
	color: #fff;
}

/* 分享对话框样式 */
.share-content {
	padding: 20px 0;
}

.share-link-section {
	margin-bottom: 20px;
}

.section-title {
	font-size: 16px;
	font-weight: 600;
	color: #333;
	margin-bottom: 15px;
}

.share-input {
	margin-bottom: 15px;
}

.copy-btn {
	width: 100%;
}

.qr-code-section {
	margin-top: 25px;
	padding-top: 25px;
	border-top: 1px solid #eee;
}

.qr-code {
	width: 200px;
	height: 200px;
	margin: 0 auto;
	display: block;
}
</style>
