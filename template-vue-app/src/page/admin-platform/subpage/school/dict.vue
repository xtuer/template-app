<template>
    <div>
        <Row>
            <Col span="12">
                <!-- 上传 Excel 字典数据文件按钮 -->
                <Upload
                    :format="['xlsx']"
                    :max-size="20480"
                    :on-success="handleUploadSuccess"
                    :on-error="handleUploadError"
                    :on-format-error="handleUploadFormatError"
                    :on-exceeded-size="handleUploadMaxSize"
                    :before-upload="handleBeforeUpload"
                    :show-upload-list="false"
                    :action="uploaDictsdUrl"
                    accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet">
                    <Button type="ghost" icon="ios-cloud-upload-outline">导入字典数据</Button>
                </Upload>
            </Col>
            <Col span="12">
                <!-- 字典分类过滤器，用于根据分类查询字典 -->
                <Select v-model="selectedDictType" clearable placeholder="所有分类" style="width:200px; float: right;" @on-change="changeDictType">
                    <Option v-for="type in dictTypes" :value="type" :key="type">{{ type }}</Option>
                </Select>
            </Col>
        </Row>

        <!-- 显示字典的表格 -->
        <Table :columns="columns" :data="dicts" style="margin: 10px 0;"></Table>

        <center>
            <!-- 加载更多字典数据的按钮 -->
            <Button v-show="moreButtonVisible" type="dashed" @click="getDicts">更多 <Icon type="ios-more"/></Button>
        </center>
    </div>
</template>
<script>
    import DictDao from '@/../static/js/dao/DictDao';

    export default {
        data() {
            return {
                pageSize: 2,
                pageNumber: 1,
                moreButtonVisible: true, // 加载更多按钮是否可见 (当加载最后一页后隐藏)
                dictTypes: [],           // 字典的分类
                selectedDictType: '',    // 选中的字典分类，加载的时候加载此分类下的字典
                uploaDictsdUrl: Urls.FORM_DICTS_IMPORT, // 上传导入字典的 URL
                columns: [ // 字典表的列名
                    { title: '编码', key: 'code' },
                    { title: '值',   key: 'value' },
                    { title: '分类', key: 'type' },
                    { title: '描述', key: 'description' },
                ],
                dicts: [] // 字典表的数据
            };
        },
        created() {
            // Mount 后加载字典分类和前 N 个字典
            this.getDicts();
            this.getDictTypes();
        },
        methods: {
            // 向服务器请求第 this.pageNumber 页的字典数据
            getDicts() {
                // 1. 发送请求前显示进度条
                // 2. 发送请求
                // 3. 请求成功，收到响应: dicts.length 大于 0 说明有数据，添加到 dicts，等于 0 则没有数据，隐藏更多按钮
                // 4. 请求完成隐藏进度条

                this.$Loading.start();
                DictDao.findByType(this.selectedDictType, this.pageNumber, this.pageSize, (dicts) => {
                    if (dicts.length > 0) {
                        this.pageNumber++;
                        this.dicts.push(...dicts); // 数组合并
                    } else {
                        // 没有更多数据则隐藏 '更多...' 按钮并提示用户
                        this.moreButtonVisible = false;
                        this.$Message.info('没有更多数据了');
                    }
                }, () => {
                    this.$Loading.finish();
                });
            },
            // 向服务器请求字典的分类
            getDictTypes() {
                DictDao.findTypes(types => this.dictTypes.push(...types));
            },
            // 改变了字典的分类时重新请求数据
            changeDictType() {
                this.dicts = [];
                this.pageNumber = 1;
                this.moreButtonVisible = true;
                this.getDicts();
            },
            // [1] 上传前打开进度条，上传处理完毕后记得关闭进度条
            handleBeforeUpload() {
                this.$Loading.start();
                return true;
            },
            // [2] 上传成功: 经过服务器端处理 (result 是服务器端返回的响应)
            handleUploadSuccess(result, file, fileList) {
                this.$Loading.finish();

                if (result.success) {
                    this.$Notice.success({ title: '导入成功' });
                } else {
                    this.$Notice.warning({ title: '字典的 Excel 模板不对' });
                    console.warn(result.data);
                }
            },
            // [3] 上传失败: 经过服务器端处理 (error 不是服务器端返回的响应)
            handleUploadError(error, file, fileList) {
                this.$Loading.error();
                this.$Notice.warning({
                    title: '导入失败'
                });
            },
            // [4] 不支持的文件格式: 前端判断
            handleUploadFormatError() {
                this.$Loading.error();
                this.$Notice.warning({
                    title: '导入失败',
                    desc: '文件格式错误，只支持 Excel xlsx'
                });
            },
            // [5] 文件查过大小限制: 前端判断
            handleUploadMaxSize() {
                this.$Loading.error();
                this.$Notice.warning({
                    title: '导入失败',
                    desc: '超出文件大小 20M'
                });
            }
        }
    };
</script>
