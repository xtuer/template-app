<!--
文件上传控件

使用方法:
    1. 引入并注册组件 FileUploader: import FileUploader from '@/components/FileUploader';
    2. 在页面中定义一个 <FileUploader ref="fileUploader" image @on-success="fileUploaded" />
    3. 再放置一个按钮，点击按钮时调用 this.$refs.fileUploader.show() 显示文件上传窗口，
       这样做是为了能够自定义点击上传文件的元素，例如可以为按钮，图片等，
       如果想使用默认的上传按钮而不是自己定义，那么使用参数 upload-button 即可
    4. 可参考 /sample/subpage/upload.vue
    5. 额外示例: <FileUploader ref="fileUploader" file multiple @on-success="fileUploaded" />

参数说明:
    multiple: 默认为 false, 为 false 表示只允许上传一个文件，为 true 允许上传多个文件
    doc:   默认为 false, 为 true 允许上传的格式为 'ppt', 'pptx', 'doc', 'docx', 'xls', 'xlsx', 'pdf'
    file:  默认为 false, 为 true 允许上传任意格式的文件，默认为 false
    image: 默认为 false, 为 true 允许上传图片格式为 'jpg', 'jpeg', 'gif', 'png'
    video: 默认为 false, 为 true 允许上传视频格式为 mp4、avi、flv、swf、wmv、mov、3gp、mpg、rmvb、mkv
    audio: 默认为 false, 为 true 允许上传视频格式为 mp3
    upload-button，默认为 false, 为 true 显示默认的上传按钮，可以使用 slot 按钮的文本

参数配置:
    主要是配置上传文件大小的限制，在 /static/js/config.js 中配置，目前有 defaultMaxSize，imageMaxSize，fileMaxSize

事件信号:
    点击确定按钮，上传完成，如果有上传的文件则发射信号 on-success:
    1. 如果 multiple 为 true 则参数为上传文件的数组
    2. 如果 multiple 为 false，则参数为单个上传文件的对象
-->
<template>
    <div class="file-uploader">
        <!-- 上传按钮，默认不显示 -->
        <Button v-if="uploadButton" type="primary" icon="ios-cloud-upload-outline" @click="showFileUploader"><slot>上传</slot></Button>

        <Modal ref="fileUploader" v-model="modalVisible" :mask-closable="false" ok-text="确定" title="上传文件" class="file-uploader-modal" @on-ok="uploadFinished">
            <Upload ref="uploader"
                :action="uploadUrl"
                :before-upload="beforeUpload"
                :on-success="onSuccess"
                :on-error="onError"
                :on-format-error="onFormatError"
                :on-exceeded-size="onExceededSize"
                :on-remove="onRemove"
                :max-size="maxSizeInKB"
                :multiple="multiple"
                :accept="accept"
                :format="format"
                with-credentials
                type="drag">
                <div style="padding: 60px 0">
                    <Icon type="ios-cloud-upload" size="52" style="color: #3399ff"></Icon>
                    <p>拖拽到或点击此处选择文件</p>
                </div>
            </Upload>

            <!-- 图片预览 -->
            <div v-show="uploadedImages.length" class="preview-images">
                <img v-for="(image, index) in uploadedImages" :src="image.url" :key="index" :title="image.filename">
            </div>

            <!-- 有上传的文件则显示分隔符，让效果更好看一些 -->
            <div v-show="uploadedFiles.length" class="separator"></div>

            <!-- 文件格式说明 -->
            <div class="description">
                <strong>上传的文件大小限制 {{ maxSizeInKB / 1024 }}MB 以内，文件格式:</strong>
                <p style="padding: 0 5px">
                    <div v-if="doc">文档：PPT (ppt，pptx)、Word (doc，docx)、Excel (xls,xlsx)、PDF</div>
                    <div v-if="file">文件：zip, rar, 7z, png, jpg, gif, mp3, mp4, pdf, doc, docx, ppt, pptx, xls, xlsx 等任意格式</div>
                    <div v-if="image">图片：png, jpg, gif</div>
                    <div v-if="video">视频：mp4、avi、flv、swf、wmv、mov、3gp、mpg、rmvb、mkv</div>
                    <div v-if="audio">音频：mp3</div>
                    <div v-if="excel">表格：Excel (xls, xlsx)</div>
                </p>
            </div>

            <Alert v-show="error" type="warning" show-icon>{{ errorText }}</Alert>
        </Modal>
    </div>
</template>

<script>
import Config from '@/../public/static/js/config';

export default {
    props: {
        multiple: { type: Boolean, default: false },
        doc  : { type: Boolean, default: false }, // 文档
        file : { type: Boolean, default: false }, // 文件
        image: { type: Boolean, default: false }, // 图片
        video: { type: Boolean, default: false }, // 视频
        audio: { type: Boolean, default: false }, // 音频
        excel: { type: Boolean, default: false }, // Excel
        uploadButton: { type: Boolean, default: false }, // 是否显示默认的上传按钮
    },
    data() {
        return {
            modalVisible : false,   // 是否显示上传对话框
            error        : false,   // 是否显示错误信息
            errorText    : 'Error', // 错误的文本
            uploadUrl    : Urls.FORM_UPLOAD_TEMPORARY_FILE, // 上传文件服务器端处理的 URL
            uploadedFiles: [],      // 成功上传的文件

            docs  : ['ppt', 'pptx', 'doc', 'docx', 'xls', 'xlsx', 'pdf'], // 文档文件格式
            excels: ['xls', 'xlsx'], // Excel
            images: ['jpg', 'jpeg', 'gif', 'png', 'bmp'], // 图片文件格式
            videos: ['mp4', 'avi', 'flv', 'swf', 'wmv', 'mov', '3gp', 'mpg', 'rmvb', 'mkv'], // 视频文件格式
            audios: ['mp3'], // 音频文件格式
        };
    },
    computed: {
        maxSizeInKB() {
            // 配置上传文件的大小，越在下面的优先级越大 (MB -> KB):
            // 1. 默认使用 defaultMaxSize
            // 2. 如果上传类型为 file 则使用 fileMaxSize
            // 3. 如果上传类型为 image 则使用 imageMaxSize
            if (this.file) {
                return Config.fileMaxSize * 1024;
            }
            if (this.image) {
                return Config.imageMaxSize * 1024;
            }

            return Config.defaultMaxSize * 1024;
        },
        accept() {
            const accepts = [];

            if (this.doc) {
                const docs = this.docs.map(type => '.' + type);
                accepts.push(...docs);
            }
            if (this.excel) {
                const xlss = this.excels.map(type => '.' + type);
                accepts.push(...xlss);
            }
            if (this.image) {
                const images = this.images.map(type => '.' + type);
                accepts.push(...images);
            }
            if (this.video) {
                const videos = this.videos.map(type => '.' + type);
                accepts.push(...videos);
            }
            if (this.audio) {
                const audios = this.audios.map(type => '.' + type);
                accepts.push(...audios);
            }
            if (this.file) {
                accepts.length = 0;
            }

            return accepts.join(',');
        },
        format() {
            const formats = [];

            if (this.doc) {
                formats.push(...this.docs);
            }
            if (this.excel) {
                formats.push(...this.excels);
            }
            if (this.image) {
                formats.push(...this.images);
            }
            if (this.video) {
                formats.push(...this.videos);
            }
            if (this.audio) {
                formats.push(...this.audios);
            }
            if (this.file) {
                formats.length = 0;
            }

            return formats;
        },
        // 上传的文件中的图片
        uploadedImages() {
            const images = [];

            for (const file of this.uploadedFiles) {
                if (Utils.isImage(file.filename)) {
                    images.push(file);
                }
            }

            return images;
        }
    },
    methods: {
        // 显示上传对话框，清空上次的数据
        show() {
            this.clearFiles();
            this.modalVisible = true;
        },
        // 显示上传对话框
        showFileUploader() {
            this.show();
        },
        // 清空文件
        clearFiles() {
            this.uploadedFiles = [];
            this.$refs.uploader.clearFiles();
        },
        // 点击确定按钮，上传完成，如果有上传的文件则发射信号 on-success
        uploadFinished() {
            if (this.uploadedFiles.length === 0) {
                return;
            }

            if (this.multiple) {
                this.$emit('on-success', this.uploadedFiles);
            } else {
                this.$emit('on-success', this.uploadedFiles[0]);
            }
        },
        // [1] 上传: 前隐藏上次的错误信息，如果只上传单个文件，还需要删除上次上传的文件
        beforeUpload() {
            this.hideError();

            if (!this.multiple) {
                this.clearFiles();
            }

            return true;
        },
        // [2] 上传成功: 经过服务器端处理 (result 是服务器端返回的响应)
        onSuccess(result, file, fileList) {
            if (result.success) {
                const fileFromServer = result.data; // 上传成功后服务器返回的文件信息
                fileFromServer.uid = file.uid;
                this.uploadedFiles.push(result.data);
            } else {
                console.warn(result.data);
                this.showError('上传失败，详细错误信息请查看控制台输出');
            }
        },
        // [3] 上传失败: 文件超过大小限制，前端判断
        onExceededSize(file) {
            this.showError(`文件 ${file.name} 的大小超过 ${this.maxSizeInKB / 1024}MB`);
        },
        // [4] 上传失败: 经过服务器端处理 (error 没带错误信息，没啥用)
        onError(error, file, fileList, args) {
            this.showError('上传失败');
        },
        // [5] 上传失败: 不支持的文件格式，前端判断
        onFormatError() {
            this.showError('文件格式不支持');
        },
        // [6] 移除文件: 以最新的 fileList 为准
        onRemove(file, fileList) {
            // file 为被删除的文件
            // uploadedFiles 只保存没有被删除的文件
            const remained = this.uploadedFiles.filter(uploadedFile => {
                return fileList.find(f => { return f.uid === uploadedFile.uid; });
            });

            this.uploadedFiles = [];
            this.uploadedFiles.push(...remained);
        },
        // 隐藏错误提示
        hideError() {
            this.error = false;
        },
        // 显示错误提示
        showError(text) {
            this.error = true;
            this.errorText = text;
        }
    }
};
</script>

<style lang="scss">
.file-uploader-modal {
    /* 上传列表 */
    .ivu-upload-list-file {
        font-size: 14px;

        /* Icon 的大小 */
        span i {
            width: 14px;
            height: 14px;
        }
    }

    .separator {
        margin: $gap 0;
        border-bottom: 1px solid $separatorColor;
    }

    .description {
        color: $iconColor;
        margin: $gap 0;
    }

    /* 图片预览 */
    .preview-images {
        display: flex;
        flex-wrap: wrap;
        margin-top: $gap;

        img {
            width: 20%; /* 每行 5 张 */
            height: 80px;
        }
    }
}
</style>
