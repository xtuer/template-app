<!--
文件上传控件

参数说明:
    doc   : 默认为 false, 为 true 允许上传的格式为 'ppt', 'pptx', 'doc', 'docx', 'xls', 'xlsx', 'pdf'
    file  : 默认为 false, 为 true 允许上传任意格式的文件
    excel : 默认为 false, 为 true 允许上传 Excel 文件
    image : 默认为 false, 为 true 允许上传图片格式为 'jpg', 'jpeg', 'gif', 'png'
    video : 默认为 false, 为 true 允许上传视频格式为 mp4、avi、flv、swf、wmv、mov、3gp、mpg、rmvb、mkv
    audio : 默认为 false, 为 true 允许上传视频格式为 mp3
    multiple   : 默认为 false, 为 false 表示只允许上传一个文件，为 true 允许上传多个文件
    button-hide: 默认为 false, 为 false 显示默认的上传按钮，为 true 则隐藏，可以使用默认 slot 修改按钮的文本

参数配置:
    主要是配置上传文件大小的限制，在 /static-p/js/config.js 中配置，目前有 defaultMaxSize，imageMaxSize，fileMaxSize
    export default {
        // 上传的文件大小单位为 MB
        defaultMaxSize: 100,
        imageMaxSize  : 10,
        fileMaxSize   : 100,
    };

事件信号:
    点击确定按钮，上传完成，如果有上传的文件则发射信号 on-success:
    1. 如果 multiple 为 true 则参数为上传文件的数组
    2. 如果 multiple 为 false，则参数为单个上传文件的对象

Slot:
    匿名 slot，定义上传按钮的名字

使用案例:
一、使用默认上传按钮:
    1. <FileUpload/>
    2. <FileUpload image multiple @on-success="imageUploaded">上传图片</FileUpload>

二、不用默认上传按钮:
    1. 在页面中定义一个 FileUpload，不显示上传按钮
       <FileUpload ref="fileUpload" image button-hide @on-success="fileUploaded"/>
    2. 再放置一个元素如 <div>，点击时显示文件上传窗口，这样做是为了能够自定义点击上传文件的元素，例如可以为按钮，图片等
       <div @click="$refs.fileUpload.show()">上传合同文件</div>
-->
<template>
    <div class="file-upload">
        <!-- 上传按钮，默认显示 -->
        <Button v-if="!buttonHide" type="primary" icon="ios-cloud-upload-outline" @click="show">
            <slot>上传文件</slot>
        </Button>

        <Modal v-model="modalVisible" :mask-closable="false" :styles="{top: '60px', marginBottom: '40px'}"
            ok-text="确定" title="上传文件" class="file-upload-modal" @on-ok="finishUpload">
            <Upload ref="upload"
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
                    :headers="{'X-Requested-With': 'XMLHttpRequest'}"
                    with-credentials
                    type="drag">
                <div style="padding: 60px 0">
                    <Icon type="ios-cloud-upload" size="52" style="color: #3399ff"></Icon>
                    <p>点击或者拖拽文件到此上传</p>
                </div>
            </Upload>

            <!-- 图片预览 -->
            <div v-show="uploadedImages.length" class="preview-images">
                <div v-for="(image, index) in uploadedImages"
                        :key="index"
                        :title="image.filename"
                        :style="{ backgroundImage: `url(${image.url})` }"
                        class="preview">
                </div>
            </div>

            <!-- 有上传的文件则显示分隔符，让效果更好看一些 -->
            <div v-show="uploadedFiles.length" class="separator"></div>

            <!-- 上传说明 -->
            <div class="description">
                <strong>文件大小不超过 {{ maxSizeInKB / 1024 }}MB，格式:</strong>
                <div style="padding: 5px 0">
                    <div v-if="doc">文档：PPT (ppt，pptx)、Word (doc，docx)、Excel (xls,xlsx)、PDF</div>
                    <div v-if="file || !accept">文件：zip, rar, 7z, png, jpg, gif, mp3, mp4, pdf, doc, docx, ppt, pptx, xls, xlsx 等任意格式</div>
                    <div v-if="image">图片：png, jpg, gif</div>
                    <div v-if="video">视频：mp4、avi、flv、swf、wmv、mov、3gp、mpg、rmvb、mkv</div>
                    <div v-if="audio">音频：mp3</div>
                    <div v-if="excel">表格：Excel (xls, xlsx)</div>
                </div>
            </div>

            <!-- 显示上传错误 -->
            <Alert v-show="error" type="warning" show-icon>{{ error }}</Alert>
        </Modal>
    </div>
</template>

<script>
import Config from '@/../public/static-p/js/config';

export default {
    props: {
        doc   : { type: Boolean, default: false }, // 文档
        file  : { type: Boolean, default: false }, // 文件
        excel : { type: Boolean, default: false }, // Excel
        image : { type: Boolean, default: false }, // 图片
        video : { type: Boolean, default: false }, // 视频
        audio : { type: Boolean, default: false }, // 音频
        multiple  : { type: Boolean, default: false }, // 是否允许上传多个文件
        buttonHide: { type: Boolean, default: false }, // 是否显示默认的上传按钮
    },
    data() {
        return {
            modalVisible: false, // 是否显示上传对话框
            error       : null,  // 错误信息
            upload      : null,  // iView Upload 组件
            uploadUrl   : Urls.FORM_UPLOAD_TEMPORARY_FILE, // 上传文件服务器端处理的 URL

            // 文件格式
            docs  : ['ppt', 'pptx', 'doc', 'docx', 'xls', 'xlsx', 'pdf'], // 文档文件
            excels: ['xls', 'xlsx'], // Excel
            images: ['jpg', 'jpeg', 'gif', 'png', 'bmp'], // 图片文件
            videos: ['mp4', 'avi', 'flv', 'swf', 'wmv', 'mov', '3gp', 'mpg', 'rmvb', 'mkv'], // 视频文件
            audios: ['mp3'], // 音频文件
        };
    },
    mounted() {
        this.upload = this.$refs.upload;
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
            let accepts = [];

            this.doc   && accepts.push(...this.docs.map(type => '.' + type));
            this.excel && accepts.push(...this.excels.map(type => '.' + type));
            this.image && accepts.push(...this.images.map(type => '.' + type));
            this.video && accepts.push(...this.videos.map(type => '.' + type));
            this.audio && accepts.push(...this.audios.map(type => '.' + type));
            accepts = this.file ? [] : accepts; // file 为 true 时不限制文件格式

            return accepts.join(',');
        },
        format() {
            let formats = [];

            this.doc   && formats.push(...this.docs);
            this.excel && formats.push(...this.excels);
            this.image && formats.push(...this.images);
            this.video && formats.push(...this.videos);
            this.audio && formats.push(...this.audios);
            formats = this.file ? [] : formats; // file 为 true 时不限制文件格式

            return formats;
        },
        // 上传完成的文件 (保存的是服务器端返回的文件信息)
        uploadedFiles() {
            if (this.upload) {
                // created 或者 mounted 之前会执行这个函数，那时 this.upload 还没有赋值，不判断会报错
                return this.upload.fileList.filter(file => file.status === 'finished').map(file => file.response.data);
            } else {
                return [];
            }
        },
        // 上传完成的图片
        uploadedImages() {
            return this.uploadedFiles.filter(file => Utils.isImage(file.filename));
        },
    },
    methods: {
        // 显示上传对话框
        show() {
            this.clearFiles(); // 清空上次的数据
            this.modalVisible = true;
        },
        // 清空文件
        clearFiles() {
            this.upload.clearFiles();
        },
        // 点击确定按钮，上传完成，如果有上传的文件则发射信号 on-success
        finishUpload() {
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
            if (!result.success) {
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
            // Donothing
        },
        // 隐藏错误提示
        hideError() {
            this.error = null;
        },
        // 显示错误提示
        showError(text) {
            this.error = text;
        },
    }
};
</script>

<style lang="scss">
.file-upload {
    display: inline-block;
}

.file-upload-modal {
    .ivu-modal-body {
        display: grid;
        grid-gap: 12px;
    }

    /* 上传列表 */
    .ivu-upload-list-file {
        font-size: 14px;

        /* Icon 的大小 */
        span i {
            width : 14px;
            height: 14px;
        }
    }

    .separator {
        border-bottom: 1px solid #efefef;
    }

    .description {
        color: #808695;
    }

    /* 图片预览 */
    .preview-images {
        display: grid;
        grid-template-columns: repeat(5, 1fr);
        grid-gap: 12px;

        .preview {
            background-repeat  : no-repeat;
            background-position: center;
            background-size    : cover;

            height: 60px;
            border-radius: 4px;
            box-shadow   : 0 0 2px #ccc; /* box-shadow: 0 0 1px #bbb; */
        }
    }

    /* 进度条样式 */
    .ivu-progress-show-info .ivu-progress-outer {
        padding-right: 0;
        margin-right: 0;
    }

    .ivu-progress-success .ivu-progress-text {
        display: none;
    }
}
</style>
