#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
API文档生成脚本
从Spring Boot Controller代码中提取接口信息，生成完善的Markdown格式API文档

功能：
1. 解析@Controller、@RestController、@RequestMapping等注解
2. 提取@ApiOperation、@ApiParam等Swagger注解
3. 解析方法注释（JavaDoc）
4. 分析请求参数（@RequestParam、@PathVariable、@RequestBody）
5. 分析响应类型
6. 生成结构化的Markdown文档
"""

import os
import re
import json
from datetime import datetime
from pathlib import Path
from collections import defaultdict
from dataclasses import dataclass, field
from typing import List, Dict, Optional, Tuple


@dataclass
class ApiParameter:
    """API参数"""
    name: str
    param_type: str  # query, path, body, header
    data_type: str
    required: bool = True
    description: str = ""
    example: str = ""
    is_array: bool = False
    sub_params: List['ApiParameter'] = field(default_factory=list)


@dataclass
class ApiResponse:
    """API响应"""
    name: str
    data_type: str
    description: str = ""
    example: str = ""
    is_array: bool = False
    sub_params: List['ApiResponse'] = field(default_factory=list)


@dataclass
class ApiEndpoint:
    """API接口"""
    name: str
    method: str  # GET, POST, PUT, DELETE
    path: str
    description: str = ""
    parameters: List[ApiParameter] = field(default_factory=list)
    responses: List[ApiResponse] = field(default_factory=list)
    tags: List[str] = field(default_factory=list)
    deprecated: bool = False


@dataclass
class Controller:
    """控制器"""
    name: str
    base_path: str
    description: str = ""
    endpoints: List[ApiEndpoint] = field(default_factory=list)
    tags: List[str] = field(default_factory=list)


class JavaControllerParser:
    """Java Controller解析器"""

    def __init__(self, java_file: str):
        self.java_file = java_file
        self.content = self._read_file()
        self.class_name = ""
        self.base_path = ""
        self.class_description = ""
        self.class_tags = []

    def _read_file(self) -> str:
        with open(self.java_file, 'r', encoding='utf-8') as f:
            return f.read()

    def parse(self) -> Optional[Controller]:
        """解析Controller文件"""
        # 提取类名
        class_match = re.search(r'(?:public\s+)?class\s+(\w+Controller)', self.content)
        if not class_match:
            return None

        self.class_name = class_match.group(1)

        # 提取类级别的@RequestMapping
        request_mapping = re.search(r'@RequestMapping\s*\(\s*["\']([^"\']+)["\']', self.content)
        self.base_path = request_mapping.group(1) if request_mapping else ""

        # 提取类注释
        self._extract_class_description()

        # 提取@Api tags
        api_match = re.search(r'@Api\s*\([^)]*tags\s*=\s*\[?"([^"\]]+)"\]?\s*\)?', self.content)
        if api_match:
            self.class_tags = [api_match.group(1)]

        # 解析所有端点
        endpoints = self._parse_endpoints()

        return Controller(
            name=self.class_name,
            base_path=self.base_path,
            description=self.class_description,
            endpoints=endpoints,
            tags=self.class_tags
        )

    def _extract_class_description(self):
        """提取类级别的注释"""
        # 匹配类声明前的JavaDoc注释
        pattern = r'/\*\*\s*\n((?:\s*\*[^/]*\n)*)\s*\*/\s*(?:@[\w]+\s*\([^)]*\)\s*)*(?:public\s+)?class\s+\w+'
        match = re.search(pattern, self.content)
        if match:
            javadoc = match.group(1)
            # 清理注释符号
            lines = []
            for line in javadoc.strip().split('\n'):
                line = line.strip()
                if line.startswith('*'):
                    line = line[1:].strip()
                if line and not line.startswith('@'):
                    lines.append(line)
            self.class_description = ' '.join(lines)

    def _parse_endpoints(self) -> List[ApiEndpoint]:
        """解析所有API端点"""
        endpoints = []

        # 首先找到所有方法定义及其位置
        # 使用更简单的方法：先匹配JavaDoc和注解，然后匹配方法签名
        # 方法签名匹配：返回类型 + 方法名 + (参数) - 使用[\s\S]来匹配包括换行在内的所有字符
        method_pattern = r'''
            (/\*\*[\s\S]*?\*/)?\s*                                          # JavaDoc注释（可选）
            ((?:@\w+(?:\([^)]*\))?\s*)*)                                    # 所有注解
            (?:public\s+)?(\w+(?:<[\w\s,<>]+>)?)\s+                         # 返回类型
            (\w+)\s*\(([\s\S]*?)\)                                          # 方法名和参数（支持多行）
        '''

        for match in re.finditer(method_pattern, self.content, re.VERBOSE):
            javadoc = match.group(1) or ""
            all_annotations = match.group(2) or ""
            return_type = match.group(3)
            method_name = match.group(4)
            params_str = match.group(5)

            # 检查是否包含HTTP方法注解
            http_method = None
            path = ""

            for method in ['Get', 'Post', 'Put', 'Delete', 'Patch']:
                # 匹配带括号和不带括号的情况
                mapping_pattern = rf'@{method}Mapping(?:\s*\(([^)]*)\))?'
                mapping_match = re.search(mapping_pattern, all_annotations)
                if mapping_match:
                    http_method = method.upper()
                    # 提取路径（可能没有）
                    annotation_content = mapping_match.group(1) or ""
                    path_match = re.search(r'["\']([^"\']+)["\']', annotation_content)
                    if path_match:
                        path = path_match.group(1)
                    break

            if not http_method:
                continue

            # 解析描述
            description = self._extract_description(javadoc, all_annotations)

            # 如果没有描述，尝试从方法名推断
            if not description or description == "暂无描述":
                description = self._infer_description_from_method(method_name)

            # 解析参数
            parameters = self._parse_parameters(params_str, javadoc)

            # 解析响应
            responses = self._parse_response(return_type)

            # 检查是否废弃
            deprecated = '@Deprecated' in all_annotations

            endpoint = ApiEndpoint(
                name=self._generate_endpoint_name(method_name, description),
                method=http_method,
                path=path,
                description=description,
                parameters=parameters,
                responses=responses,
                deprecated=deprecated
            )

            endpoints.append(endpoint)

        return endpoints

    def _infer_description_from_method(self, method_name: str) -> str:
        """从方法名推断描述"""
        # 常见方法名映射
        method_desc_map = {
            'get': '获取',
            'list': '获取列表',
            'find': '查询',
            'query': '查询',
            'search': '搜索',
            'create': '创建',
            'add': '添加',
            'save': '保存',
            'update': '更新',
            'modify': '修改',
            'delete': '删除',
            'remove': '移除',
            'cancel': '取消',
            'upload': '上传',
            'download': '下载',
            'send': '发送',
            'receive': '接收',
            'process': '处理',
            'execute': '执行',
            'register': '注册',
            'login': '登录',
            'logout': '登出',
            'verify': '验证',
            'check': '检查',
            'validate': '校验',
            'confirm': '确认',
            'approve': '审批',
            'reject': '拒绝',
            'submit': '提交',
            'publish': '发布',
            'unpublish': '取消发布',
            'enable': '启用',
            'disable': '禁用',
            'start': '启动',
            'stop': '停止',
            'pause': '暂停',
            'resume': '恢复',
            'reset': '重置',
            'clear': '清空',
            'flush': '刷新',
            'sync': '同步',
            'export': '导出',
            'import': '导入',
            'copy': '复制',
            'move': '移动',
            'sort': '排序',
            'filter': '筛选',
            'count': '统计',
            'calculate': '计算',
            'analyze': '分析',
            'generate': '生成',
            'build': '构建',
            'parse': '解析',
            'format': '格式化',
            'convert': '转换',
            'transform': '转换',
            'merge': '合并',
            'split': '拆分',
            'join': '加入',
            'leave': '离开',
            'accept': '接受',
            'decline': '拒绝',
            'complete': '完成',
            'finish': '结束',
            'rollback': '回滚',
            'reorder': '再来一单',
            'pay': '支付',
            'refund': '退款',
            'withdraw': '提现',
            'deposit': '充值',
            'transfer': '转账',
            'balance': '余额',
            'recharge': '充值',
        }

        # 将驼峰命名转换为空格分隔
        words = []
        current_word = ""
        for char in method_name:
            if char.isupper():
                if current_word:
                    words.append(current_word.lower())
                current_word = char
            else:
                current_word += char
        if current_word:
            words.append(current_word.lower())

        # 尝试匹配第一个单词
        if words:
            first_word = words[0]
            if first_word in method_desc_map:
                action = method_desc_map[first_word]
                # 组合剩余单词作为对象
                obj = ''.join(words[1:]) if len(words) > 1 else ''
                # 将驼峰对象转换为中文描述
                obj_desc = self._convert_camel_to_chinese(obj)
                return f"{action}{obj_desc}" if obj_desc else action

        return "暂无描述"

    def _convert_camel_to_chinese(self, text: str) -> str:
        """将驼峰命名转换为中文描述"""
        # 常见业务词汇映射
        word_map = {
            'user': '用户',
            'users': '用户',
            'merchant': '商家',
            'merchants': '商家',
            'order': '订单',
            'orders': '订单',
            'dish': '菜品',
            'dishes': '菜品',
            'menu': '菜单',
            'category': '分类',
            'categories': '分类',
            'address': '地址',
            'addresses': '地址',
            'cart': '购物车',
            'favorite': '收藏',
            'favorites': '收藏',
            'collection': '收藏',
            'collections': '收藏',
            'review': '评价',
            'reviews': '评价',
            'comment': '评论',
            'comments': '评论',
            'message': '消息',
            'messages': '消息',
            'notification': '通知',
            'notifications': '通知',
            'chat': '聊天',
            'group': '群组',
            'groups': '群组',
            'session': '会话',
            'sessions': '会话',
            'history': '历史',
            'record': '记录',
            'records': '记录',
            'statistics': '统计',
            'stats': '统计',
            'report': '报表',
            'reports': '报表',
            'config': '配置',
            'configs': '配置',
            'setting': '设置',
            'settings': '设置',
            'preference': '偏好',
            'preferences': '偏好',
            'profile': '个人资料',
            'avatar': '头像',
            'password': '密码',
            'phone': '手机号',
            'email': '邮箱',
            'code': '验证码',
            'captcha': '验证码',
            'token': '令牌',
            'auth': '授权',
            'oauth': 'OAuth授权',
            'login': '登录',
            'logout': '登出',
            'register': '注册',
            'info': '信息',
            'detail': '详情',
            'details': '详情',
            'list': '列表',
            'page': '分页',
            'status': '状态',
            'count': '数量',
            'amount': '金额',
            'balance': '余额',
            'wallet': '钱包',
            'payment': '支付',
            'refund': '退款',
            'withdraw': '提现',
            'coupon': '优惠券',
            'coupons': '优惠券',
            'discount': '折扣',
            'promotion': '促销',
            'banner': '横幅',
            'announcement': '公告',
            'announcements': '公告',
            'notice': '通知',
            'tutorial': '教程',
            'tutorials': '教程',
            'help': '帮助',
            'feedback': '反馈',
            'complaint': '投诉',
            'suggestion': '建议',
            'log': '日志',
            'logs': '日志',
            'error': '错误',
            'warning': '警告',
            'exception': '异常',
            'cache': '缓存',
            'file': '文件',
            'files': '文件',
            'image': '图片',
            'images': '图片',
            'video': '视频',
            'audio': '音频',
            'document': '文档',
            'documents': '文档',
            'upload': '上传',
            'download': '下载',
            'import': '导入',
            'export': '导出',
            'backup': '备份',
            'restore': '恢复',
            'sync': '同步',
            'async': '异步',
            'batch': '批量',
            'bulk': '批量',
            'all': '所有',
            'one': '单个',
            'by': '按',
            'with': '带',
            'for': '为',
            'to': '到',
            'from': '从',
            'id': 'ID',
            'ids': 'ID',
            'name': '名称',
            'title': '标题',
            'content': '内容',
            'description': '描述',
            'type': '类型',
            'tag': '标签',
            'tags': '标签',
            'label': '标签',
            'labels': '标签',
            'key': '键',
            'value': '值',
            'data': '数据',
            'result': '结果',
            'response': '响应',
            'request': '请求',
            'param': '参数',
            'params': '参数',
            'query': '查询',
            'filter': '筛选',
            'sort': '排序',
            'order': '排序',
            'page': '分页',
            'size': '大小',
            'limit': '限制',
            'offset': '偏移',
            'cursor': '游标',
            'next': '下一个',
            'previous': '上一个',
            'first': '第一个',
            'last': '最后一个',
            'current': '当前',
            'total': '总计',
            'sum': '合计',
            'avg': '平均',
            'min': '最小',
            'max': '最大',
            'start': '开始',
            'end': '结束',
            'begin': '开始',
            'finish': '结束',
            'create': '创建',
            'update': '更新',
            'delete': '删除',
            'remove': '移除',
            'add': '添加',
            'edit': '编辑',
            'modify': '修改',
            'change': '更改',
            'set': '设置',
            'get': '获取',
            'fetch': '获取',
            'load': '加载',
            'save': '保存',
            'store': '存储',
            'persist': '持久化',
            'flush': '刷新',
            'clear': '清空',
            'reset': '重置',
            'init': '初始化',
            'destroy': '销毁',
            'dispose': '释放',
            'close': '关闭',
            'open': '打开',
            'enable': '启用',
            'disable': '禁用',
            'activate': '激活',
            'deactivate': '停用',
            'lock': '锁定',
            'unlock': '解锁',
            'block': '屏蔽',
            'unblock': '取消屏蔽',
            'ban': '封禁',
            'unban': '解封',
            'approve': '审批',
            'reject': '拒绝',
            'confirm': '确认',
            'cancel': '取消',
            'undo': '撤销',
            'redo': '重做',
            'rollback': '回滚',
            'recover': '恢复',
            'restore': '还原',
            'retry': '重试',
            'skip': '跳过',
            'pause': '暂停',
            'resume': '继续',
            'stop': '停止',
            'start': '启动',
            'restart': '重启',
            'reboot': '重启',
            'refresh': '刷新',
            'reload': '重新加载',
            'rebuild': '重新构建',
            'regenerate': '重新生成',
            'recalculate': '重新计算',
            'reindex': '重新索引',
            'reorder': '重新排序',
            'reorganize': '重新组织',
            'restructure': '重构',
            'refactor': '重构',
            'optimize': '优化',
            'improve': '改进',
            'enhance': '增强',
            'upgrade': '升级',
            'downgrade': '降级',
            'migrate': '迁移',
            'convert': '转换',
            'transform': '转换',
            'translate': '翻译',
            'transliterate': '音译',
            'transcribe': '转录',
            'parse': '解析',
            'serialize': '序列化',
            'deserialize': '反序列化',
            'encode': '编码',
            'decode': '解码',
            'encrypt': '加密',
            'decrypt': '解密',
            'hash': '哈希',
            'sign': '签名',
            'verify': '验证',
            'validate': '校验',
            'check': '检查',
            'test': '测试',
            'debug': '调试',
            'trace': '追踪',
            'track': '跟踪',
            'monitor': '监控',
            'observe': '观察',
            'watch': '监视',
            'listen': '监听',
            'subscribe': '订阅',
            'unsubscribe': '取消订阅',
            'publish': '发布',
            'broadcast': '广播',
            'notify': '通知',
            'alert': '告警',
            'warn': '警告',
            'error': '错误',
            'fail': '失败',
            'succeed': '成功',
            'complete': '完成',
            'finish': '完成',
            'done': '完成',
            'pending': '待处理',
            'processing': '处理中',
            'running': '运行中',
            'ready': '就绪',
            'waiting': '等待',
            'queued': '排队中',
            'scheduled': '已调度',
            'cancelled': '已取消',
            'expired': '已过期',
            'timeout': '超时',
            'retry': '重试',
        }

        # 尝试直接匹配
        if text.lower() in word_map:
            return word_map[text.lower()]

        # 尝试拆分驼峰并逐个匹配
        words = []
        current_word = ""
        for char in text:
            if char.isupper():
                if current_word:
                    words.append(current_word.lower())
                current_word = char
            else:
                current_word += char
        if current_word:
            words.append(current_word.lower())

        result = []
        for word in words:
            if word in word_map:
                result.append(word_map[word])
            else:
                result.append(word)

        return ''.join(result)

    def _parse_mapping_annotation(self, annotation: str) -> Tuple[str, str]:
        """解析@RequestMapping等注解，返回HTTP方法和路径"""
        if not annotation:
            return None, None

        # 提取HTTP方法
        method_match = re.match(r'@(Get|Post|Put|Delete|Patch)Mapping', annotation)
        if method_match:
            http_method = method_match.group(1).upper()
        else:
            http_method = "GET"

        # 提取路径
        path_match = re.search(r'["\']([^"\']+)["\']', annotation)
        path = path_match.group(1) if path_match else ""

        return http_method, path

    def _extract_description(self, javadoc: str, all_annotations: str) -> str:
        """提取接口描述"""
        description = ""

        # 优先从@ApiOperation提取
        op_match = re.search(r'@ApiOperation\s*\(\s*value\s*=\s*"([^"]+)"', all_annotations)
        if op_match:
            description = op_match.group(1)

        # 尝试获取notes
        notes_match = re.search(r'notes\s*=\s*"([^"]+)"', all_annotations)
        if notes_match:
            description = description + " - " + notes_match.group(1) if description else notes_match.group(1)

        # 如果没有@ApiOperation，从JavaDoc提取
        if not description and javadoc:
            # 清理JavaDoc格式
            lines = []
            in_javadoc = False
            for line in javadoc.split('\n'):
                line = line.strip()
                if line.startswith('/**'):
                    in_javadoc = True
                    line = line[3:].strip()
                if line.endswith('*/'):
                    line = line[:-2].strip()
                    in_javadoc = False
                if line.startswith('*'):
                    line = line[1:].strip()
                # 跳过@param、@return等标签
                if line.startswith('@'):
                    break
                if line:
                    lines.append(line)
            description = ' '.join(lines)

        return description

    def _parse_parameters(self, params_str: str, javadoc: str) -> List[ApiParameter]:
        """解析方法参数"""
        parameters = []

        if not params_str.strip():
            return parameters

        # 解析@param注释
        param_docs = {}
        if javadoc:
            for match in re.finditer(r'@param\s+(\w+)\s+(.+?)(?=@param|@return|@throws|\*/)', javadoc, re.DOTALL):
                param_name = match.group(1)
                param_desc = match.group(2).strip()
                # 清理换行和星号
                param_desc = re.sub(r'\s*\*\s*', ' ', param_desc).strip()
                param_docs[param_name] = param_desc

        # 解析参数列表
        # 处理泛型参数
        params_str = re.sub(r'<[^>]+>', 'GENERIC', params_str)

        for param in params_str.split(','):
            param = param.strip()
            if not param:
                continue

            # 解析@RequestParam
            request_param = re.search(
                r'@RequestParam\s*\(\s*(?:value\s*=\s*)?["\']?(\w+)["\']?\s*(?:,\s*required\s*=\s*(\w+))?',
                param
            )
            if request_param:
                param_name = request_param.group(1)
                required = request_param.group(2) != 'false' if request_param.group(2) else True
                data_type = self._extract_param_type(param)

                parameters.append(ApiParameter(
                    name=param_name,
                    param_type='query',
                    data_type=data_type,
                    required=required,
                    description=param_docs.get(param_name, '')
                ))
                continue

            # 解析@PathVariable
            path_var = re.search(r'@PathVariable\s*\(\s*(?:value\s*=\s*)?["\']?(\w+)["\']?', param)
            if path_var:
                param_name = path_var.group(1)
                data_type = self._extract_param_type(param)

                parameters.append(ApiParameter(
                    name=param_name,
                    param_type='path',
                    data_type=data_type,
                    required=True,
                    description=param_docs.get(param_name, '')
                ))
                continue

            # 解析@RequestBody
            if '@RequestBody' in param:
                param_name = self._extract_param_name(param)
                data_type = self._extract_param_type(param)
                desc = param_docs.get(param_name, '')

                parameters.append(ApiParameter(
                    name=param_name,
                    param_type='body',
                    data_type=data_type,
                    required=True,
                    description=desc if desc else '请求体'
                ))
                continue

            # 解析@RequestHeader
            header_param = re.search(r'@RequestHeader\s*\(\s*(?:value\s*=\s*)?["\']?(\w+)["\']?', param)
            if header_param:
                param_name = header_param.group(1)
                data_type = self._extract_param_type(param)

                parameters.append(ApiParameter(
                    name=param_name,
                    param_type='header',
                    data_type=data_type,
                    required=True,
                    description=param_docs.get(param_name, '')
                ))

        return parameters

    def _extract_param_type(self, param: str) -> str:
        """提取参数类型"""
        # 移除注解
        param = re.sub(r'@\w+\s*\([^)]*\)', '', param)
        param = re.sub(r'@\w+', '', param)

        parts = param.strip().split()
        if len(parts) >= 2:
            return parts[-2].replace('GENERIC', '<T>')
        return "string"

    def _extract_param_name(self, param: str) -> str:
        """提取参数名"""
        parts = param.strip().split()
        if parts:
            return parts[-1]
        return "body"

    def _parse_response(self, return_type: str) -> List[ApiResponse]:
        """解析响应类型"""
        responses = []

        if 'ResponseResult' in return_type or 'Result' in return_type:
            responses.append(ApiResponse(
                name='success',
                data_type='boolean',
                description='是否成功',
                example='true'
            ))
            responses.append(ApiResponse(
                name='code',
                data_type='string',
                description='业务状态码',
                example='200'
            ))
            responses.append(ApiResponse(
                name='message',
                data_type='string',
                description='提示信息',
                example='操作成功'
            ))
            responses.append(ApiResponse(
                name='data',
                data_type='object',
                description='业务数据'
            ))
        elif 'Page' in return_type or 'IPage' in return_type:
            responses.append(ApiResponse(
                name='records',
                data_type='array',
                description='数据列表'
            ))
            responses.append(ApiResponse(
                name='total',
                data_type='integer',
                description='总记录数',
                example='100'
            ))
            responses.append(ApiResponse(
                name='current',
                data_type='integer',
                description='当前页码',
                example='1'
            ))
            responses.append(ApiResponse(
                name='size',
                data_type='integer',
                description='每页大小',
                example='10'
            ))
        else:
            responses.append(ApiResponse(
                name='data',
                data_type=return_type,
                description='返回数据'
            ))

        return responses

    def _generate_endpoint_name(self, method_name: str, description: str) -> str:
        """生成端点名称"""
        if description:
            # 取描述的第一句话
            first_sentence = re.split(r'[。.，,！!？?]', description)[0]
            if len(first_sentence) > 20:
                first_sentence = first_sentence[:20]
            return first_sentence

        # 从方法名生成
        name = re.sub(r'([A-Z])', r' \1', method_name)
        return name.strip()


class ApiDocGenerator:
    """API文档生成器"""

    def __init__(self, controllers: List[Controller]):
        self.controllers = controllers
        self.service_base_url = "http://localhost:7777/api"
        self.current_base_path = ""  # 当前正在处理的Controller的base_path

    def generate_markdown(self, output_file: str):
        """生成Markdown文档"""
        lines = []

        # 文档头部
        lines.append("# 后端API文档")
        lines.append("")
        lines.append(f"生成时间：{datetime.now().strftime('%Y-%m-%d %H:%M')}")
        lines.append(f"接口总数：{sum(len(c.endpoints) for c in self.controllers)}")
        lines.append(f"服务基址：`{self.service_base_url}`")
        lines.append("")

        # 通用响应结构
        lines.extend(self._generate_common_response())
        lines.append("")

        # 通用异常响应
        lines.extend(self._generate_error_responses())
        lines.append("")

        # 目录
        lines.extend(self._generate_toc())
        lines.append("")

        # 各Controller的接口详情
        for controller in self.controllers:
            self.current_base_path = controller.base_path  # 设置当前Controller的base_path
            lines.extend(self._generate_controller_section(controller))
            lines.append("")

        # 写入文件
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write('\n'.join(lines))

    def _generate_common_response(self) -> List[str]:
        """生成通用响应结构"""
        return [
            "## 通用响应结构",
            "",
            "| 参数名 | 类型 | 说明 | 示例值 |",
            "| --- | --- | --- | --- |",
            "| `success` | boolean | 是否成功 | `true` |",
            "| `code` | string | 业务状态码 | `\"200\"` |",
            "| `message` | string | 提示信息 | `\"操作成功\"` |",
            "| `data` | object/array | 业务数据体 | `{}` |",
        ]

    def _generate_error_responses(self) -> List[str]:
        """生成通用异常响应"""
        return [
            "## 通用异常响应",
            "",
            "| 错误码 | 错误信息 | 异常场景 |",
            "| --- | --- | --- |",
            "| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |",
            "| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |",
            "| `403` | 无权限访问 | 角色权限不足 |",
            "| `404` | 资源不存在 | 路径参数对应记录不存在 |",
            "| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |",
            "| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |",
        ]

    def _generate_toc(self) -> List[str]:
        """生成目录"""
        lines = ["## 目录", ""]

        for controller in self.controllers:
            anchor = self._to_anchor(controller.name)
            count = len(controller.endpoints)
            desc = f"（{controller.description}）" if controller.description else ""
            lines.append(f"- [{controller.name}](#{anchor}){desc}（{count} 个接口）")

        return lines

    def _generate_controller_section(self, controller: Controller) -> List[str]:
        """生成Controller章节"""
        lines = []

        # 标题
        lines.append(f"## {controller.name}")
        if controller.description:
            lines.append(f"*{controller.description}*")
        lines.append("")

        # 各接口
        for i, endpoint in enumerate(controller.endpoints, 1):
            lines.extend(self._generate_endpoint_section(endpoint, i))
            lines.append("")

        return lines

    def _generate_endpoint_section(self, endpoint: ApiEndpoint, index: int) -> List[str]:
        """生成接口章节"""
        lines = []

        # 接口标题
        deprecated_mark = " ~~(已废弃)~~" if endpoint.deprecated else ""
        lines.append(f"### {index}. {endpoint.name}{deprecated_mark}")
        lines.append("")

        # 基本信息
        lines.append("#### 接口基本信息")
        lines.append("")
        lines.append("| 项目 | 内容 |")
        lines.append("| --- | --- |")
        lines.append(f"| 接口名称 | {endpoint.name} |")
        # 正确拼接路径：base_path + endpoint.path
        full_path = self.current_base_path + endpoint.path if self.current_base_path else endpoint.path
        lines.append(f"| 请求地址 | `{self.service_base_url}{full_path}` |")
        lines.append(f"| 请求方式 | `{endpoint.method}` |")
        lines.append(f"| 接口描述 | {endpoint.description or '暂无描述'} |")
        lines.append("")

        # 请求参数
        lines.append("#### 请求参数")
        lines.append("")

        if endpoint.parameters:
            lines.append("| 参数名 | 类型 | 必填 | 位置 | 说明 |")
            lines.append("| --- | --- | --- | --- | --- |")

            for param in endpoint.parameters:
                required = "是" if param.required else "否"
                location = self._get_param_location(param.param_type)
                desc = param.description or "-"
                lines.append(f"| `{param.name}` | {param.data_type} | {required} | {location} | {desc} |")
        else:
            lines.append("无请求参数。")

        lines.append("")

        # 响应参数
        lines.append("#### 响应参数")
        lines.append("")

        if endpoint.responses:
            lines.append("| 参数名 | 类型 | 说明 |")
            lines.append("| --- | --- | --- |")

            for resp in endpoint.responses:
                lines.append(f"| `{resp.name}` | {resp.data_type} | {resp.description or '-'} |")
        else:
            lines.append("默认返回通用响应结构。")

        lines.append("")

        # 异常响应（简化）
        lines.append("#### 异常响应")
        lines.append("")
        lines.append("参见[通用异常响应](#通用异常响应)。")
        lines.append("")

        # 请求示例
        lines.append("#### 请求示例")
        lines.append("")
        lines.append("```json")
        if endpoint.parameters:
            example = {}
            for param in endpoint.parameters:
                if param.param_type == 'body':
                    example = {"// 请求体": "根据实际业务填写"}
                else:
                    example[param.name] = self._get_example_value(param.data_type)
            lines.append(json.dumps(example, ensure_ascii=False, indent=2))
        else:
            lines.append("{}")
        lines.append("```")
        lines.append("")

        # 响应示例
        lines.append("#### 响应示例")
        lines.append("")
        lines.append("```json")
        example_response = {
            "success": True,
            "code": "200",
            "message": "操作成功",
            "data": {}
        }
        lines.append(json.dumps(example_response, ensure_ascii=False, indent=2))
        lines.append("```")

        return lines

    def _to_anchor(self, name: str) -> str:
        """转换为锚点"""
        return name.lower().replace('_', '-')

    def _get_param_location(self, param_type: str) -> str:
        """获取参数位置说明"""
        locations = {
            'query': '查询参数',
            'path': '路径参数',
            'body': '请求体',
            'header': '请求头'
        }
        return locations.get(param_type, param_type)

    def _get_example_value(self, data_type: str) -> any:
        """获取示例值"""
        type_lower = data_type.lower()

        if 'string' in type_lower or 'str' in type_lower:
            return "示例值"
        elif 'integer' in type_lower or 'int' in type_lower or 'long' in type_lower:
            return 1
        elif 'boolean' in type_lower or 'bool' in type_lower:
            return True
        elif 'double' in type_lower or 'float' in type_lower or 'decimal' in type_lower or 'bigdecimal' in type_lower:
            return 99.9
        elif 'list' in type_lower or 'array' in type_lower:
            return []
        elif 'date' in type_lower:
            return datetime.now().strftime('%Y-%m-%d')
        elif 'localdatetime' in type_lower:
            return datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        else:
            return {}


def main():
    """主函数"""
    # Controller目录
    controller_dir = Path("/Users/nickxiao/JasEatsChoice/JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller")

    # 输出文件
    output_file = Path("/Users/nickxiao/JasEatsChoice/docs/api/后端API文档_完善版.md")

    print("=" * 60)
    print("API文档生成器")
    print("=" * 60)
    print(f"Controller目录: {controller_dir}")
    print(f"输出文件: {output_file}")
    print()

    # 解析所有Controller
    controllers = []

    for java_file in sorted(controller_dir.glob("*.java")):
        print(f"解析: {java_file.name}")
        parser = JavaControllerParser(str(java_file))
        controller = parser.parse()

        if controller and controller.endpoints:
            controllers.append(controller)
            print(f"  - 发现 {len(controller.endpoints)} 个接口")

    print()
    print(f"共解析 {len(controllers)} 个Controller")
    print(f"共发现 {sum(len(c.endpoints) for c in controllers)} 个接口")
    print()

    # 生成文档
    print("正在生成文档...")
    generator = ApiDocGenerator(controllers)
    generator.generate_markdown(str(output_file))

    print(f"文档已生成: {output_file}")
    print("=" * 60)


if __name__ == "__main__":
    main()
