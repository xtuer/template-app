<template>
    <div class="layout">
        <Sider :style="{position: 'fixed', height: '100vh', left: 0, overflow: 'auto'}">
            <Menu active-name="1-2" theme="dark" width="auto" :open-names="['1', '2']">
                <Submenu name="1">
                    <template slot="title">
                        <Icon type="ios-navigate" />订单中心
                    </template>
                    <MenuItem name="1-1">我的订单</MenuItem>
                    <MenuItem name="1-2">我的活动</MenuItem>
                    <MenuItem name="1-3">订单晒价</MenuItem>
                </Submenu>
                <Submenu name="2">
                    <template slot="title">
                        <Icon type="ios-keypad" />关注中心
                    </template>
                    <MenuItem name="2-1">关注商品</MenuItem>
                    <MenuItem name="2-2">关注店铺</MenuItem>
                </Submenu>
                <Submenu name="3">
                    <template slot="title">
                        <Icon type="ios-analytics" />资产中心
                    </template>
                    <MenuItem name="3-1">我的余额</MenuItem>
                    <MenuItem name="3-2">我的积分</MenuItem>
                </Submenu>
            </Menu>
        </Sider>
        <Layout :style="{marginLeft: '200px'}">
            <Header>
                <div class="user">
                    公孙二狗
                    <Avatar src="https://i.loli.net/2017/08/21/599a521472424.jpg" size="large" />
                </div>
            </Header>
            <Content :style="{padding: '0 16px 16px'}">
                <Breadcrumb :style="{margin: '16px 0'}">
                    <BreadcrumbItem>Home</BreadcrumbItem>
                    <BreadcrumbItem>Components</BreadcrumbItem>
                    <BreadcrumbItem>Layout</BreadcrumbItem>
                </Breadcrumb>
                <Card dis-hover style="height: 600px">
                    Content
                </Card>
            </Content>
        </Layout>
    </div>
</template>

<script>
    export default {};
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
</style>
