<!--
使用方法:
1. 不使用数据初始化编辑器: <Richtext/>
2. 要使用数据初始化编辑器:
    <Richtext v-model="html" :min-height="200" inline/>
    data() {
        return {
            html: '<p>Hello</p>',
        };
    }
3. 使用自定义工具栏: 目前提供了 2 中工具栏
    <Richtext :toolbar="0"/>
4. 获取编辑器的内容: 编辑后传入的 html 会自动同步编辑的内容
-->
<template>
    <div ref="editor" class="richtext">
        <!-- 富文本编辑器 -->
        <div :id="editorId"></div>

        <!-- 右上角的关闭按钮 -->
        <Icon v-if="closable" class="close-button" type="md-close-circle" size="16" @click="close" />

        <!-- 每个编辑器都有自己的上传组件 -->
        <FileUpload ref="fileUpload" v-bind="uploadProps" @on-success="fileUploaded"/>
    </div>
</template>

<script>
import FileUpload from '@/components/FileUpload.vue';

export default {
    props: {
        html     : { type: String,  default: '<p></p>' }, // HTML 内容
        inline   : { type: Boolean, default: false },     // 是否 inline 模式
        minHeight: { type: Number,  default: 101 },       // 最小高度
        toolbar  : { type: Number,  default: 0, validator(value) { return value >= 0 && value <= 1; } }, // 工具栏的下标: 0 或 1
        closable : { type: Boolean, default: false },     // 是否可关闭
        readOnly : { type: Boolean, default: false },     // 是否只读
        placeholder: { type: String, default: ' '  },     // 占位内容
    },
    model: {
        prop : 'html',
        event: 'text-changed' // 编辑器的内容发生变化后触发, 参数为编辑的 HTML
    },
    components: { FileUpload },
    data() {
        return {
            editor     : null,
            toolbars   : [],
            baseToolbar: 'styleselect | bold italic underline strikethrough subscript superscript',
            uploadMode : 0,  // 上传模式: 0(image), 1(mp3, mp4), 2(file)
        };
    },
    mounted() {
        // 创建各种场景使用的 toolbar
        const toolbar1 = `${this.baseToolbar} image media file fitb code`; // 普通场景使用
        const toolbar2 = `${this.baseToolbar} image media fitb code`;      // 编辑题目使用
        this.toolbars  = [toolbar1, toolbar2];

        // 懒加载 TinyMCE
        Utils.loadJs(Urls.TINY_MCE).then(() => {
            this.initEditor();
        });
    },
    methods: {
        // 点击关闭按钮发射 close 信号
        close() {
            this.$emit('close');
        },
        // 设置 HTML
        setHtml(html) {
            this.editor.setContent(html || '<p></p>');
        },
        // 创建编辑器
        initEditor() {
            const self = this;

            tinymce.init({
                selector     : `#${this.editorId}`,
                inline       : this.inline,
                min_height   : this.minHeight,
                toolbar      : this.toolbars[this.toolbar],
                readonly     : this.readOnly,
                language     : 'zh_CN',
                branding     : false,
                elementpath  : false,
                statusbar    : false,
                relative_urls: false, // 不把绝对路径转换为相对路径
                menu         : {},
                placeholder  : this.placeholder,
                plugins      : 'paste code placeholder',
                paste_as_text: true,     // 去掉 Word 的格式: https://www.tiny.cloud/docs/plugins/paste/
                paste_data_images: true, // Word 里复制的图片保存为 Base64 格式
                setup: function(editor) {
                    // 创建工具栏按钮
                    self.addToolbarButtons(editor);

                    // 编辑器内容发生变化后更新 html 的内容
                    editor.on('change keyup', () => {
                        self.$emit('text-changed', editor.getContent());
                    });
                },
                // 粘贴的回调函数
                paste_preprocess: function(plugin, args) {
                    // 上传粘贴的图片
                    if (args.content.startsWith('<img')) {
                        self.uploadPastedImage(args);
                    }
                }
            }).then(editors => {
                this.editor = editors[0];
                this.editor.setContent(this.html);
                this.editor.fire('selectionchange', {}); // 触发更新 placeholder

                // inline 时的最小高度
                if (self.inline && self.minHeight !== 101) {
                    document.getElementById(self.editorId + '').style.minHeight = self.minHeight + 'px';
                }
            });
        },

        addToolbarButtons(editor) {
            // 添加填空按钮
            editor.addButton('fitb', { icon: 'line', title: '插入填空', onclick: () => {
                editor.insertContent(' <abbr class="fitb">________</abbr> ');
            } });

            // 添加上传图片按钮
            editor.addButton('image', { icon: 'image', title: '插入图片', onclick: () => {
                this.uploadMode = 0;
                this.showFileUpload();
            } });

            // 添加上传 MP3, MP4 按钮
            editor.addButton('media', { icon: 'media', title: '插入 MP3 或 MP4', onclick: () => {
                this.uploadMode = 1;
                this.showFileUpload();
            } });

            // 添加上传文件按钮
            editor.addButton('file', { icon: 'upload', title: '上传文件', onclick: () => {
                this.uploadMode = 2;
                this.showFileUpload();
            } });
        },
        // 显示文件上传对话框
        showFileUpload() {
            this.$refs.fileUpload.show();
        },
        fileUploaded(file) {
            // [2] 插入文件到文本框
            const url      = file.url;
            const filename = file.filename;

            if (this.uploadProps.image) {
                // [2.1] 上传图片插入文本框
                const width  = file.imageWidth;
                const height = file.imageHeight;
                this.editor.insertContent(`<img src="${url}" title="${filename}" align="top" style="max-width: 100%" data-width="${width}" data-height="${height}">`);
            } else if (this.uploadProps.audio && Utils.isMp3(url)) {
                // [2.2] 上传 MP3
                this.editor.insertContent(`&nbsp;<audio controls><source src="${url}"></audio>&nbsp;`);
            } else if (this.uploadProps.video && Utils.isMp4(url)) {
                // [2.3] 上传 MP4
                this.editor.insertContent(`<div><video controls style="max-width: 100%;"><source src="${url}"></video></div> `);
            } else if (this.uploadProps.file) {
                // [2.4] 上传文件
                this.editor.insertContent(`&nbsp;<a href="${url}" class="file">${filename}</a>&nbsp;`);
            }
        },
        // 上传粘贴的图片
        uploadPastedImage(args) {
            // 1. 显示上传中提示
            // 2. 给粘贴的图片设置一个唯一 ID
            // 3. 使用 XMLHttpRequest 获取 Blob URL 源数据
            // 4. 上传 Blob 图片
            // 5. 上传完成后设置使用图片的 ID 查找图片，替换它的 src 为上传得到的 src

            const self = this;

            // [1] 显示上传中提示
            const msg = self.$Message.loading({ content: '上传中...', duration: 0 });

            // [2] 给粘贴的图片设置一个唯一 ID
            const imageId = 'pasted-image-' + Date.now();
            const xhr = new XMLHttpRequest();

            // [3] 使用 XMLHttpRequest 获取 Blob URL 源数据
            xhr.onreadystatechange = function() {
                if (this.readyState === 4 && this.status === 200) {
                    const imageBlob = this.response;
                    const imageType = imageBlob.type;
                    let   imageName = null;

                    // 根据文件的类型确定文件名
                    if (imageType.includes('png')) {
                        imageName = `${imageId}.png`;
                    } else if (imageType.includes('gif')) {
                        imageName = `${imageId}.gif`;
                    } else if (imageType.includes('bmp')) {
                        imageName = `${imageId}.bmp`;
                    } else {
                        imageName = `${imageId}.jpg`;
                    }

                    // [4] 上传 Blob 图片
                    const form = new FormData();
                    form.append('file', imageBlob, imageName);

                    // 使用 Axios 上传图片
                    axios({
                        method : 'POST',
                        url    : Urls.FORM_UPLOAD_TEMPORARY_FILE,
                        data   : form,
                        headers: { 'Content-Type': 'multipart/form-data' }
                    }).then(result => {
                        // [5] 上传完成后设置使用图片的 ID 查找图片，替换它的 src 为上传得到的 src
                        const file   = result.data.data;
                        const src    = file.url;
                        const width  = file.imageWidth;
                        const height = file.imageHeight;

                        // 不知道为啥，不放到 nextTick 有些浏览器里替换图片会有问题
                        self.$nextTick(() => {
                            // 只修改 Richtext 里面图片的 src
                            const image = self.$refs.editor.querySelector(`#${imageId}`)
                                       || self.$refs.editor.querySelector(`#${self.editorId}_ifr`).contentWindow.document.querySelector(`#${imageId}`);

                            if (width && height) {
                                image.width  = width;
                                image.height = height;
                            }

                            image.onload = msg; // 图片加载结束时关闭加载中提示
                            image.setAttribute('align', 'top');
                            image.src = src;
                        });
                    }).catch(err => {
                        console.log('上传失败！');
                    });

                    // 使用 jQuery 上传图片
                    // $.ajax({
                    //     type: 'POST',
                    //     url : Urls.FORM_UPLOAD_TEMPORARY_FILE,
                    //     data: form,
                    //     processData: false,
                    //     contentType: false,
                    // }).done(function(result) {
                    //     // [5] 上传完成后设置使用图片的 ID 查找图片，替换它的 src 为上传得到的 src
                    //     const file   = result.data;
                    //     const width  = file.imageWidth;
                    //     const height = file.imageHeight;

                    //     const imgId  = '#' + imageId;
                    //     const $image = $(imgId).length > 0 ? $(imgId) : $(`#${self.editorId}_ifr`).contents().find(imgId);
                    //     $image.attr('src', file.url).width(width).height(height);
                    // });
                }
            };

            // 粘贴文件得到 blob
            args.content = args.content.replace('<img', `<img id="${imageId}"`); // 给上传的图片增加唯一 ID
            xhr.open('GET', args.content.split('"')[3]); // blob:http://localhost:8888/da126298-1b6b-4dfb-8a92-2e3ccbee611d
            xhr.responseType = 'blob';
            xhr.send();
        },
    },
    computed: {
        // 使用时间戳和随机数生成 editorId
        editorId() {
            const time = Date.now();
            const rand = Math.floor(Math.random() * 100000000);
            return `editor-${time}-${rand}`;
        },
        // 根据上传的模式计算上传组件绑定的属性, 限制上传的文件类型
        uploadProps() {
            // 0: 上传图片, 1: 上传 MP3 和 MP4, 2: 上传文件
            return {
                buttonHide: true,
                image : this.uploadMode === 0, // 上传图片插入文本框
                audio : this.uploadMode === 1, // 上传音频插入文本框
                video : this.uploadMode === 1, // 上传视频插入文本框
                file  : this.uploadMode === 2, // 上传文件插入文本框
            };
        }
    },
    beforeDestroy() {
        // 销毁编辑器
        this.editor.remove();
    },
    watch: {
        // 外部修改 v-model 绑定的 html 的值时更新编辑器的内容
        html(newValue, oldValue) {
            if (this.editor && newValue !== this.editor.getContent()) {
                this.editor.setContent(newValue || '<p></p>');
            }
        }
    },
};
</script>

<style lang="scss">
.richtext {
    position: relative;

    img {
        max-width: 100%;
    }

    .close-button {
        position: absolute;
        top:   3px;
        right: 3px;
        cursor: pointer;
        color: $iconColor;

        &:hover {
            color: $primaryColor;
        }
    }

    .file-upload {
        display: none;
    }

    // 微调 Full featured 的编辑器阴影, 工具栏阴影
    .mce-tinymce {
        // box-shadow: 0 0px 1px rgba(0, 0, 0, 0.25);

        .mce-top-part::before {
            box-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
        }
    }

    // inline 模式的边框和得到焦点的样式
    .mce-content-body {
        padding: 8px;
        border: 1px solid #DDD;
        border-radius: 3px;

        &:hover {
            border-color: #47a4f5;
            transition: all;
            @include defaultAnimation;
        }

        &.mce-edit-focus {
            outline: none;
            border-color: #47a4f5;
            box-shadow: 2px 2px 3px rgba(0, 0, 0, 0.1) inset;
        }

        &.plugin-placeholder:before {
            color: #4545456e;
            -webkit-margin-before: 0em;
        }
    }

    .mce-notification {
        display: none;
    }
}

// 工具栏样式: 缩小字体, 修改按钮的 hover 背景色等
.mce-panel {
    border: none !important;

    .mce-toolbar .mce-btn-group {
        padding: 0;

        &:not(:first-child) {
            margin-left: 0;
        }

        .mce-btn {
            margin-left: 0;

            &:hover {
                border-color: transparent;
                background: #808695;
                color: white;

                .mce-ico, button {
                    color: white;
                }
            }

            .mce-ico {
                font-size: 14px;
            }
        }
    }
}
</style>
