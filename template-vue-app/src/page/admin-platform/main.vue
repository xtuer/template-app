<template>
    <div class="layout">
        <Sider :style="{position: 'fixed', height: '100vh', left: 0, overflow: 'auto'}">
            <div class="logo"><img src="/static/img/logo.png"></div>

            <!-- [1] active-name 为当前高亮的 MenuItem 的 name -->
            <!-- [2] :open-names 为要展开的 Submenu 的 name 的数组 -->
            <!-- [3] @on-select 为点击 MenuItem 后的回调，参数为 MenuItem 的 name，一般设置为要跳转的路径 -->
            <Menu :open-names="['1']" active-name="school" theme="dark" width="auto" @on-select="navigateTo">
                <Submenu name="1">
                    <template slot="title"><Icon type="ios-navigate"/>学校管理</template>
                    <MenuItem name="school">学校</MenuItem>
                    <MenuItem name="school-meta">学校元数据</MenuItem>
                </Submenu>
                <Submenu name="2">
                    <template slot="title"><Icon type="ios-keypad"/>关注中心</template>
                    <MenuItem name="2-1">关注商品</MenuItem>
                    <MenuItem name="2-2">关注店铺</MenuItem>
                </Submenu>
                <Submenu name="3">
                    <template slot="title"><Icon type="ios-analytics"/>资产中心</template>
                    <MenuItem name="3-1">我的余额</MenuItem>
                    <MenuItem name="3-2">我的积分</MenuItem>
                </Submenu>
            </Menu>
        </Sider>
        <Layout :style="{marginLeft: '200px'}">
            <Header>
                <Button type="dashed" shape="circle" icon="android-arrow-back" @click="goBack"></Button>
                <div class="user">
                    公孙二狗
                    <Avatar src="https://i.loli.net/2017/08/21/599a521472424.jpg"/>
                </div>
            </Header>
            <Content :style="{padding: '16px 16px 16px'}">
                <!-- <Breadcrumb :style="{margin: '16px 0'}">
                    <BreadcrumbItem>Home</BreadcrumbItem>
                    <BreadcrumbItem>Components</BreadcrumbItem>
                    <BreadcrumbItem>Layout</BreadcrumbItem>
                </Breadcrumb> -->
                <Card dis-hover style="min-height: 600px">
                    <router-view></router-view>
                </Card>
            </Content>
        </Layout>
    </div>
</template>

<script>
    export default {
        methods: {
            navigateTo: function(name) {
                this.$router.push({name: name});
            },
            goBack() {
                this.$router.go(-1);
            }
        }
    };
</script>

<style scoped lang="scss">
    .layout {
        background: #f5f7f9;
        position: relative;
        overflow: hidden;
    }
    .ivu-layout-header {
        padding: 0 16px;
        background: #fff;
        box-shadow: 0 2px 3px 2px rgba(0, 0, 0, .02);
        .user {
            float: right;
            font-weight: bold;
            font-size: 14px;
        }
    }
    .logo {
        padding: 8px;
        display: flex;
        justify-content: center;

        img {
            width: auto;
            height: 44px;
        }
    }
</style>
