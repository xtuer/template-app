<template>
    <div>
        <Button type="primary" @click="addSchool">添加学校</Button>
        <Table :columns="columns" :data="schools" style="margin: 10px 0;"></Table>
        <center>
            <Page :total="100" :page-size="20" size="small"></Page>
        </center>
    </div>
</template>

<script>
    const schools = [];
    for (let i = 0; i < 10; ++i) {
        schools.push({
            name: '贵阳六中-' + i,
            province: '贵州/贵阳',
            type: '初中',
            url:  'http://ebag.gylzh.cn',
            info: '2346/13/945/831'
        });
    }

    export default {
        mounted() {
            // this.$Message.info('Loading schools');
        },
        data() {
            return {
                columns: [
                    { title: '学校名称', key: 'name' },
                    { title: '省份',    key: 'province' },
                    { title: '教育类型', key: 'type' },
                    { title: '站点地址', key: 'url' },
                    { title: '用户数/教师数/学生数/家长数', key: 'info' },
                    { title: '操作', key: 'action', width: 160, align: 'center',
                        // 编辑和删除按钮
                        render: (h, params) => {
                            return (
                                <div class="cell-button-container">
                                    <i-button type="primary" size="small" onClick={()=>{this.editSchool(params.index)}}   icon="edit">编辑</i-button>
                                    <i-button type="error"   size="small" onClick={()=>{this.deleteSchool(params.index)}} icon="android-delete">删除</i-button>
                                </div>
                            );
                        }
                    }
                ],
                schools: schools
            };
        },
        methods: {
            // 添加新学校
            addSchool() {
                this.$router.push({
                    name: 'school-editor', params: {id: 12}
                });
            },
            editSchool(index) {
                this.$Message.info('Edit: ' + index);
            },
            deleteSchool(index) {
                this.$Message.info('Delete: ' + index);
                this.schools.splice(index, 1);
            }
        }
    };
</script>

<style lang="scss">
    .app-main {
        width: 100%;
        height: 100%;
    }

    .cell-button-container button:first-child {
        margin-right: 5px;
    }

    /* 鼠标放到行上才显示按钮 */
    /* .cell-button-container {
        display: none;
    }

    .ivu-table-row:hover .cell-button-container {
        display: block;
    } */
</style>
