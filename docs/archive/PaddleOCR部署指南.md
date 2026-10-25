# PaddleOCR 部署指南

## 📋 目录

- [部署架构](#部署架构)
- [方案一：Docker部署（推荐）](#方案一docker部署推荐)
- [方案二：本地部署（开发环境）](#方案二本地部署开发环境)
- [测试验证](#测试验证)
- [集成到Java后端](#集成到java后端)
- [常见问题排查](#常见问题排查)

---

## 部署架构

```
┌─────────────────┐
│  Java后端      │  主应用 (Spring Boot)
│  :8080         │
└────────┬────────┘
         │ HTTP API调用
         ↓
┌─────────────────┐
│  Python服务     │  PaddleOCR服务 (FastAPI)
│  :8001         │
└─────────────────┘
```

---

## 方案一：Docker部署（推荐）

### 1. 创建项目目录结构

```bash
# 在服务器上创建目录
mkdir -p /opt/paddle-ocr-service
cd /opt/paddle-ocr-service

# 创建以下目录结构
paddle-ocr-service/
├── app/
│   ├── main.py              # FastAPI主程序
│   ├── ocr_service.py       # OCR业务逻辑
│   └── requirements.txt     # Python依赖
├── models/                  # 模型文件目录（可选，首次运行自动下载）
├── uploads/                # 临时图片上传目录
├── Dockerfile              # Docker镜像构建文件
├── docker-compose.yml       # Docker编排文件
└── config.py               # 配置文件
```

### 2. 创建 Dockerfile

```dockerfile
# Dockerfile
FROM python:3.9-slim

# 设置工作目录
WORKDIR /app

# 安装系统依赖
RUN apt-get update && apt-get install -y \
    libgomp1 \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libgl1-mesa-glx \
    && rm -rf /var/lib/apt/lists/*

# 复制依赖文件
COPY requirements.txt .

# 安装Python依赖
RUN pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 复制应用代码
COPY app/ /app/

# 创建必要的目录
RUN mkdir -p /app/uploads /app/models

# 暴露端口
EXPOSE 8001

# 启动命令
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001", "--workers", "2"]
```

### 3. 创建 requirements.txt

```txt
# requirements.txt
fastapi==0.104.1
uvicorn[standard]==0.24.0
python-multipart==0.0.6
pillow==10.1.0
numpy==1.24.3
opencv-python-headless==4.8.1.78
paddlepaddle==2.5.2
paddleocr==2.7.0.3
pydantic==2.5.0
python-dotenv==1.0.0
redis==5.0.1
pydantic-settings==2.1.0
```

### 4. 创建 docker-compose.yml

```yaml
version: '3.8'

services:
  paddle-ocr:
    build: .
    container_name: paddle-ocr-service
    ports:
      - "8001:8001"
    volumes:
      - ./uploads:/app/uploads
      - ./models:/root/.paddleocr  # 缓存模型文件
    environment:
      - TZ=Asia/Shanghai
      - OCR_USE_GPU=false
      - OCR_USE_MP=True
      - OCR_TOTAL_THREAD_NUM=4
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8001/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

### 5. 创建应用代码

#### app/config.py

```python
# config.py
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    # 服务配置
    APP_NAME: str = "PaddleOCR服务"
    APP_VERSION: str = "1.0.0"
    API_PREFIX: str = "/api/v1"

    # OCR配置
    OCR_USE_GPU: bool = False
    OCR_LANG: str = "ch"  # ch=中英文, en=英文
    OCR_USE_ANGLE_CLS: bool = True
    OCR_SHOW_LOG: bool = False

    # 并发配置
    OCR_USE_MP: bool = True
    OCR_TOTAL_THREAD_NUM: int = 4

    # Redis配置（可选，用于缓存）
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 0
    REDIS_PASSWORD: str = None

    # 文件配置
    MAX_UPLOAD_SIZE: int = 10 * 1024 * 1024  # 10MB
    ALLOWED_EXTENSIONS: set = {'.jpg', '.jpeg', '.png', '.bmp', '.gif'}

    class Config:
        env_file = ".env"
        case_sensitive = True

settings = Settings()
```

#### app/ocr_service.py

```python
# ocr_service.py
from paddleocr import PaddleOCR
import numpy as np
from PIL import Image
import io
import cv2
import base64
from typing import List, Dict, Optional
import hashlib
import logging

from app.config import settings

logger = logging.getLogger(__name__)

class OCRService:
    _instance: Optional['OCRService'] = None
    _ocr_engine = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._initialized = False
        return cls._instance

    def __init__(self):
        if self._initialized:
            return

        logger.info("初始化PaddleOCR引擎...")
        logger.info(f"GPU模式: {settings.OCR_USE_GPU}")
        logger.info(f"多进程模式: {settings.OCR_USE_MP}")

        self._ocr_engine = PaddleOCR(
            use_angle_cls=settings.OCR_USE_ANGLE_CLS,
            lang=settings.OCR_LANG,
            use_gpu=settings.OCR_USE_GPU,
            use_mp=settings.OCR_USE_MP,
            total_process_num=settings.OCR_TOTAL_THREAD_NUM,
            show_log=settings.OCR_SHOW_LOG
        )

        self._initialized = True
        logger.info("PaddleOCR引擎初始化完成")

    def recognize_from_file(self, image_path: str) -> Dict:
        """
        从文件路径识别
        """
        result = self._ocr_engine.ocr(image_path, cls=settings.OCR_USE_ANGLE_CLS)
        return self._parse_result(result)

    def recognize_from_bytes(self, image_bytes: bytes) -> Dict:
        """
        从字节流识别
        """
        # 转换为numpy数组
        nparr = np.frombuffer(image_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        result = self._ocr_engine.ocr(img, cls=settings.OCR_USE_ANGLE_CLS)
        return self._parse_result(result)

    def recognize_from_base64(self, base64_str: str) -> Dict:
        """
        从Base64字符串识别
        """
        # 移除base64前缀（如果有）
        if ',' in base64_str:
            base64_str = base64_str.split(',')[1]

        # 解码
        image_bytes = base64.b64decode(base64_str)
        return self.recognize_from_bytes(image_bytes)

    def _parse_result(self, result) -> Dict:
        """
        解析OCR结果
        """
        if not result or not result[0]:
            return {
                "success": False,
                "message": "未识别到任何文字",
                "data": {
                    "texts": [],
                    "fullText": "",
                    "itemCount": 0
                }
            }

        texts = []
        confidences = []

        for line in result[0]:
            # line格式: [[x1,y1], [x2,y2], [x3,y3], [x4,y4]], (text, confidence)
            box = line[0]
            text_info = line[1]

            texts.append({
                "text": text_info[0],
                "confidence": float(text_info[1]),
                "box": [[float(p[0]), float(p[1])] for p in box]
            })
            confidences.append(text_info[1])

        # 按位置排序（从上到下，从左到右）
        sorted_texts = self._sort_by_position(texts)

        # 计算平均置信度
        avg_confidence = sum(confidences) / len(confidences) if confidences else 0

        return {
            "success": True,
            "message": "识别成功",
            "data": {
                "texts": sorted_texts,
                "fullText": "\n".join([t["text"] for t in sorted_texts]),
                "itemCount": len(sorted_texts),
                "averageConfidence": round(avg_confidence, 2)
            }
        }

    def _sort_by_position(self, texts: List[Dict]) -> List[Dict]:
        """
        按位置对文字进行排序（先按Y坐标，再按X坐标）
        """
        # 首先按Y坐标分组
        y_threshold = 20  # Y坐标差异小于20像素视为同一行

        sorted_by_y = sorted(texts, key=lambda x: x["box"][0][1])

        lines = []
        current_line = [sorted_by_y[0]]
        current_y = sorted_by_y[0]["box"][0][1]

        for text in sorted_by_y[1:]:
            y = text["box"][0][1]
            if abs(y - current_y) <= y_threshold:
                current_line.append(text)
            else:
                # 同一行按X坐标排序
                lines.append(sorted(current_line, key=lambda x: x["box"][0][0]))
                current_line = [text]
                current_y = y

        # 最后一行
        if current_line:
            lines.append(sorted(current_line, key=lambda x: x["box"][0][0]))

        # 展开所有行
        result = []
        for line in lines:
            result.extend(line)

        return result

    def calculate_md5(self, image_bytes: bytes) -> str:
        """
        计算图片MD5
        """
        return hashlib.md5(image_bytes).hexdigest()


# 全局OCR服务实例
ocr_service = OCRService()
```

#### app/main.py

```python
# main.py
from fastapi import FastAPI, File, UploadFile, HTTPException, Form
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import uvicorn
import logging
import sys
from pathlib import Path

from app.config import settings
from app.ocr_service import ocr_service

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)

logger = logging.getLogger(__name__)

# 创建FastAPI应用
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="基于PaddleOCR的文字识别服务"
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境应该配置具体域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
async def health_check():
    """健康检查"""
    return {
        "status": "healthy",
        "service": settings.APP_NAME,
        "version": settings.APP_VERSION
    }


@app.post(f"{settings.API_PREFIX}/ocr/file")
async def ocr_from_file(
    file: UploadFile = File(..., description="图片文件")
):
    """
    从上传的文件中识别文字

    参数:
        file: 图片文件（支持jpg, png, bmp, gif）

    返回:
        {
            "success": true,
            "message": "识别成功",
            "data": {
                "texts": [...],
                "fullText": "完整文字",
                "itemCount": 10,
                "averageConfidence": 0.95
            }
        }
    """
    try:
        # 验证文件类型
        file_ext = Path(file.filename).suffix.lower()
        if file_ext not in settings.ALLOWED_EXTENSIONS:
            raise HTTPException(
                status_code=400,
                detail=f"不支持的文件类型。支持的类型: {settings.ALLOWED_EXTENSIONS}"
            )

        # 读取文件
        image_bytes = await file.read()

        # 检查文件大小
        if len(image_bytes) > settings.MAX_UPLOAD_SIZE:
            raise HTTPException(
                status_code=400,
                detail=f"文件过大。最大允许 {settings.MAX_UPLOAD_SIZE // (1024*1024)}MB"
            )

        # 执行OCR识别
        result = ocr_service.recognize_from_bytes(image_bytes)

        return JSONResponse(content=result)

    except Exception as e:
        logger.error(f"OCR识别失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"OCR识别失败: {str(e)}")


@app.post(f"{settings.API_PREFIX}/ocr/base64")
async def ocr_from_base64(
    image: str = Form(..., description="Base64编码的图片字符串")
):
    """
    从Base64字符串中识别文字
    """
    try:
        result = ocr_service.recognize_from_base64(image)
        return JSONResponse(content=result)

    except Exception as e:
        logger.error(f"OCR识别失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"OCR识别失败: {str(e)}")


@app.post(f"{settings.API_PREFIX}/ocr/recipe")
async def extract_recipe(
    file: UploadFile = File(..., description="菜谱图片")
):
    """
    识别菜谱图片，提取结构化信息
    """
    try:
        image_bytes = await file.read()
        ocr_result = ocr_service.recognize_from_bytes(image_bytes)

        if not ocr_result["success"]:
            return ocr_result

        texts = ocr_result["data"]["texts"]

        # 提取菜谱信息
        recipe_info = extract_recipe_info(texts)

        return {
            "success": True,
            "message": "菜谱识别成功",
            "data": {
                "ocr": ocr_result["data"],
                "recipe": recipe_info
            }
        }

    except Exception as e:
        logger.error(f"菜谱识别失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"菜谱识别失败: {str(e)}")


def extract_recipe_info(texts: list) -> dict:
    """
    从OCR结果中提取菜谱信息
    """
    import re

    # 默认值
    dish_name = "未知菜名"
    ingredients = []
    steps = []
    tags = []

    if not texts:
        return {
            "dishName": dish_name,
            "ingredients": ingredients,
            "steps": steps,
            "tags": tags
        }

    # 提取菜名（通常是第一个较大的文字）
    for i, text in enumerate(texts[:5]):  # 只看前5个
        if len(text["text"]) >= 2 and len(text["text"]) <= 10:
            dish_name = text["text"]
            break

    # 提取食材（包含常见单位）
    units = ["克", "g", "公斤", "kg", "斤", "两", "毫升", "ml", "勺", "个", "只", "片", "根", "颗"]

    for text in texts:
        content = text["text"]
        if any(unit in content for unit in units):
            # 清理格式
            clean_text = re.sub(r'\s+', ' ', content).strip()
            ingredients.append(clean_text)

    # 提取步骤（包含数字序号）
    step_pattern = r'^(\d+[\.\s、：]|第[一二三四五六七八九十\d]+步|步骤[\d]*)'
    for text in texts:
        if re.match(step_pattern, text["text"]):
            steps.append(text["text"])

    # 提取标签
    keywords = ["家常菜", "下饭菜", "快手菜", "素食", "荤菜", "汤", "甜品", "早餐", "午餐", "晚餐"]
    for text in texts:
        for keyword in keywords:
            if keyword in text["text"] and keyword not in tags:
                tags.append(keyword)

    return {
        "dishName": dish_name,
        "ingredients": ingredients,
        "steps": steps,
        "tags": tags
    }


@app.get("/")
async def root():
    """根路径"""
    return {
        "message": "PaddleOCR服务运行中",
        "version": settings.APP_VERSION,
        "endpoints": {
            "health": "/health",
            "ocr_file": "/api/v1/ocr/file",
            "ocr_base64": "/api/v1/ocr/base64",
            "ocr_recipe": "/api/v1/ocr/recipe"
        }
    }


if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8001,
        reload=True,
        workers=2
    )
```

### 6. 创建 .env 配置文件

```bash
# .env
# OCR配置
OCR_USE_GPU=false
OCR_USE_MP=true
OCR_TOTAL_THREAD_NUM=4
OCR_LANG=ch
OCR_USE_ANGLE_CLS=true
OCR_SHOW_LOG=false

# Redis配置（可选）
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=0
REDIS_PASSWORD=

# 文件配置
MAX_UPLOAD_SIZE=10485760
```

### 7. 构建并启动服务

```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f paddle-ocr

# 检查健康状态
curl http://localhost:8001/health

# 查看运行状态
docker-compose ps
```

---

## 方案二：本地部署（开发环境）

### 1. 安装 Python 3.9+

```bash
# macOS
brew install python@3.9

# Ubuntu/Debian
sudo apt update
sudo apt install python3.9 python3.9-venv python3-pip

# Windows
# 从 python.org 下载安装包
```

### 2. 创建虚拟环境

```bash
# 进入项目目录
cd /path/to/your/project/paddle-ocr-service

# 创建虚拟环境
python3.9 -m venv venv

# 激活虚拟环境
# macOS/Linux
source venv/bin/activate

# Windows
venv\Scripts\activate
```

### 3. 安装依赖

```bash
# 升级pip
pip install --upgrade pip

# 安装依赖
pip install -r requirements.txt

# 如果遇到PIL问题，安装libgl1-mesa-glx（Ubuntu）
sudo apt-get install libgl1-mesa-glx
```

### 4. 本地运行

```bash
# 方式1: 使用uvicorn直接运行
uvicorn app.main:app --host 0.0.0.0 --port 8001 --reload

# 方式2: 使用Python运行
python -m app.main

# 方式3: 多worker运行（生产环境推荐）
uvicorn app.main:app --host 0.0.0.0 --port 8001 --workers 4
```

---

## 测试验证

### 1. 健康检查

```bash
curl http://localhost:8001/health
```

**预期输出：**
```json
{
  "status": "healthy",
  "service": "PaddleOCR服务",
  "version": "1.0.0"
}
```

### 2. 文件上传测试

```bash
curl -X POST "http://localhost:8001/api/v1/ocr/file" \
  -F "file=@/path/to/test/image.jpg"
```

### 3. Python测试脚本

```python
# test_ocr.py
import requests
import base64

# 测试文件上传
def test_file_upload():
    url = "http://localhost:8001/api/v1/ocr/file"

    with open("test_image.jpg", "rb") as f:
        files = {"file": f}
        response = requests.post(url, files=files)

    print(response.json())

# 测试Base64上传
def test_base64_upload():
    url = "http://localhost:8001/api/v1/ocr/base64"

    with open("test_image.jpg", "rb") as f:
        image_base64 = base64.b64encode(f.read()).decode()

    data = {"image": image_base64}
    response = requests.post(url, data=data)

    print(response.json())

# 测试菜谱识别
def test_recipe_extraction():
    url = "http://localhost:8001/api/v1/ocr/recipe"

    with open("recipe_image.jpg", "rb") as f:
        files = {"file": f}
        response = requests.post(url, files=files)

    print(response.json())

if __name__ == "__main__":
    test_file_upload()
```

---

## 集成到Java后端

### 1. 添加配置

```yaml
# application.yml
ocr:
  service:
    url: http://localhost:8001
    enabled: true
    timeout: 30000  # 30秒
```

### 2. 创建配置类

```java
@Configuration
@ConfigurationProperties(prefix = "ocr.service")
@Data
public class OcrConfig {
    private String url;
    private Boolean enabled;
    private Integer timeout;
}
```

### 3. 创建OCR客户端

```java
@Service
public class PaddleOcrClient {

    @Autowired
    private OcrConfig ocrConfig;

    private final RestTemplate restTemplate;

    public PaddleOcrClient() {
        this.restTemplate = new RestTemplate();

        // 设置超时
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        this.restTemplate.setRequestFactory(factory);
    }

    /**
     * 识别图片文件
     */
    public OcrResult recognizeImage(MultipartFile file) throws Exception {
        if (!ocrConfig.getEnabled()) {
            throw new RuntimeException("OCR服务未启用");
        }

        String url = ocrConfig.getUrl() + "/api/v1/ocr/file";

        // 构建multipart请求
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(convertToFile(file)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request =
            new HttpEntity<>(body, headers);

        ResponseEntity<OcrResponse> response = restTemplate.postForEntity(
            url,
            request,
            OcrResponse.class
        );

        return response.getBody().getData();
    }

    /**
     * 识别菜谱
     */
    public RecipeOcrResult recognizeRecipe(MultipartFile file) throws Exception {
        String url = ocrConfig.getUrl() + "/api/v1/ocr/recipe";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(convertToFile(file)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request =
            new HttpEntity<>(body, headers);

        ResponseEntity<RecipeOcrResponse> response = restTemplate.postForEntity(
            url,
            request,
            RecipeOcrResponse.class
        );

        return response.getBody().getData();
    }

    private File convertToFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("ocr_", "_" + multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        return tempFile;
    }
}
```

---

## 常见问题排查

### 1. 内存不足

**现象：** 服务频繁重启或OOM

**解决方案：**
```yaml
# 修改docker-compose.yml，增加内存限制
services:
  paddle-ocr:
    deploy:
      resources:
        limits:
          memory: 4G
```

### 2. 首次启动慢

**现象：** 第一次请求很慢（10-30秒）

**原因：** 首次运行需要下载模型文件

**解决方案：**
```bash
# 提前下载模型
mkdir -p ~/.paddleocr/whl/det/ch/
mkdir -p ~/.paddleocr/whl/rec/ch/
mkdir -p ~/.paddleocr/whl/cls/ch/

# 或者在Docker中挂载模型目录
volumes:
  - ./models:/root/.paddleocr
```

### 3. GPU支持

**启用GPU：**
```bash
# 安装GPU版本的PaddlePaddle
pip install paddlepaddle-gpu==2.5.2

# 修改配置
OCR_USE_GPU=true
```

**前提条件：**
- NVIDIA GPU
- CUDA 11.2+
- cuDNN 8.2+

### 4. 中文乱码

**现象：** 识别的中文显示为乱码

**解决方案：**
```python
# 确保使用UTF-8编码
import sys
sys.setdefaultencoding('utf-8')

# 或者在启动时设置环境变量
# LANG=C.UTF-8 uvicorn app.main:app
```

### 5. 识别准确度低

**提升方法：**
1. 图片预处理（去噪、二值化）
2. 调整图片分辨率（建议DPI 300）
3. 使用高精度模式
4. 针对特定场景微调模型

### 6. 并发性能优化

**方法1：增加worker数量**
```bash
uvicorn app.main:app --workers 4
```

**方法2：启用多进程模式**
```python
# config.py
OCR_USE_MP=True
OCR_TOTAL_THREAD_NUM=4
```

**方法3：使用Redis缓存**
```python
# 对相同图片返回缓存结果
import hashlib

def get_cache_key(image_bytes):
    return f"ocr:result:{hashlib.md5(image_bytes).hexdigest()}"
```

---

## API文档

### 接口列表

| 接口 | 方法 | 描述 |
|------|------|------|
| `/health` | GET | 健康检查 |
| `/api/v1/ocr/file` | POST | 上传文件识别 |
| `/api/v1/ocr/base64` | POST | Base64字符串识别 |
| `/api/v1/ocr/recipe` | POST | 菜谱识别 |

### 通用响应格式

```json
{
  "success": true,
  "message": "识别成功",
  "data": {
    "texts": [
      {
        "text": "识别的文字",
        "confidence": 0.95,
        "box": [[x1, y1], [x2, y2], [x3, y3], [x4, y4]]
      }
    ],
    "fullText": "完整文字内容",
    "itemCount": 10,
    "averageConfidence": 0.92
  }
}
```

### 菜谱识别响应

```json
{
  "success": true,
  "message": "菜谱识别成功",
  "data": {
    "ocr": {...},
    "recipe": {
      "dishName": "宫保鸡丁",
      "ingredients": ["鸡肉 300克", "花生米 100克"],
      "steps": ["1. 切鸡肉", "2. 炒制"],
      "tags": ["家常菜", "下饭菜"]
    }
  }
}
```

---

## 性能参考

| 配置 | QPS | 平均延迟 | CPU | 内存 |
|------|-----|---------|-----|------|
| 单进程 | 2-3 | 300ms | 50% | 1GB |
| 4进程 | 8-10 | 500ms | 200% | 2GB |
| 8进程+GPU | 20+ | 200ms | 300% | 4GB |

---

## 版本历史

- **v1.0.0** (2025-01-31)
  - 初始版本
  - 支持中英文OCR识别
  - 支持菜谱结构化提取

---

## 参考资料

- [PaddleOCR官方文档](https://github.com/PaddlePaddle/PaddleOCR)
- [FastAPI官方文档](https://fastapi.tiangolo.com/)
- [Docker部署最佳实践](https://docs.docker.com/develop/dev-best-practices/)

---

**文档维护者：** Claude
**最后更新：** 2025-01-31
