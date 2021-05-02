var user  = 'user';
var users = 'users';
var label = '用户';
var dao   = 'UserDao'
var Users = 'Users';

var str = `
<!-- eslint-disable vue/no-parsing-error -->

<!--
搜索${label}、分页加载 (加载下一页的${label})
-->
<template>
    <div class="${users} list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
            <!-- 搜索条件 -->
            <div class="filter">
                <!-- 指定条件的搜索 -->
                <Input v-model="filter.nickname" placeholder="请输入查询条件">
                    <span slot="prepend">姓甚名谁</span>
                </Input>

                <!-- 时间范围 -->
                <DatePicker v-model="dateRange"
                            format="MM-dd"
                            separator=" 至 "
                            type="daterange"
                            data-prepend="创建时间"
                            class="date-picker"
                            split-panels
                            placeholder="请选择创建时间范围">
                </DatePicker>

                <!-- 选择条件的搜索 -->
                <Input v-model="filterValue" transfer placeholder="请输入查询条件" search enter-button @on-search="search${Users}">
                    <Select v-model="filterKey" slot="prepend">
                        <Option value="email">邮件地址</Option>
                        <Option value="phone">电话号码</Option>
                    </Select>
                </Input>
            </div>

            <!-- 其他按钮 -->
            <Button type="primary" icon="md-add">添加${label}</Button>
        </div>

        <!-- ${label}列表 -->
        <Table :data="${users}" :columns="columns" :loading="reloading" border>
            <!-- 介绍信息 -->
            <template slot-scope="{ row: ${user} }" slot="info">
                {{ ${user}.userId }}
            </template>

            <!-- 操作按钮 -->
            <template slot-scope="{ row: ${user} }" slot="action">
                <Button type="primary" size="small">编辑</Button>
                <Button type="error" size="small">删除</Button>
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMore${Users}">更多...</Button>
        </div>
    </div>
</template>

<script>
import ${dao} from '@/../public/static-p/js/dao/${dao}';

export default {
    data() {
        return {
            ${users}   : [],
            filter     : this.newFilter(), // 搜索条件
            filterKey  : 'email',  // 搜索的 Key
            filterValue: '',       // 搜索的 Value
            dateRange  : ['', ''], // 搜索的时间范围
            more     : false, // 是否还有更多${label}
            loading  : false, // 加载中
            reloading: false,
            columns  : [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { key : 'nickname', title: '名字', width: 150 },
                { slot: 'info',   title: '介绍', minWidth: 500 },
                { slot: 'action', title: '操作', width: 150, align: 'center', className: 'table-action' },
            ]
        };
    },
    mounted() {
        this.search${Users}();
    },
    methods: {
        // 搜索${label}
        search${Users}() {
            this.${users}               = [];
            this.more                   = false;
            this.reloading              = true;
            this.filter                 = { ...this.newFilter(), name: this.filter.nickname };
            this.filter[this.filterKey] = this.filterValue;

            // 如果不需要时间范围，则删除
            if (this.dateRange[0] && this.dateRange[1]) {
                this.filter.startAt = this.dateRange[0].format('yyyy-MM-dd');
                this.filter.endAt   = this.dateRange[1].format('yyyy-MM-dd');
            } else {
                this.filter.startAt = '';
                this.filter.endAt   = '';
            }

            this.fetchMore${Users}();
        },
        // 点击更多按钮加载下一页的${label}
        fetchMore${Users}() {
            this.loading = true;

            ${dao}.find${Users}(this.filter).then(${users} => {
                this.${users}.push(...${users});

                this.more      = ${users}.length >= this.filter.pageSize;
                this.loading   = false;
                this.reloading = false;
                this.filter.pageNumber++;
            });
        },
        // 新建搜索条件
        newFilter() {
            return { // 搜索条件
                // customerSn : '',
                // business: '',
                nickname  : '',
                pageSize  : 50,
                pageNumber: 1,
            };
        },
    }
};
</script>

<style lang="scss">
</style>
`;

console.log(str);
