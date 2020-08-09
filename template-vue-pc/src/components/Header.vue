<!-- Header (根据业务需求进行修改) -->
<template>
    <div class="header">
        <div class="title"><slot></slot></div>

        <!-- 用户头像和菜单 -->
        <Dropdown>
            <a href="javascript:void(0)" class="user-info">
                <Avatar :src="user.avatar || '/static-p/img/avatar.jpg'" :size="30" :bordered="false"/>
                <span class="user-name">{{ user.nickname }}</span>
                <Icon type="ios-arrow-down" style="margin-left: 8px"/>
            </a>
            <DropdownMenu slot="list" class="menu-user">
                <DropdownItem><router-link to="/user-info">个人中心</router-link></DropdownItem>
                <DropdownItem><a href="/logout">退出账号</a></DropdownItem>
            </DropdownMenu>
        </Dropdown>
    </div>
</template>

<script>
export default {
    props: {},
    data() {
        return {};
    },
    methods: {},
    computed: {
        user() {
            return {};
        }
    }
};
</script>

<style lang="scss">
.header {
    display: grid;
    grid-template-columns: 1fr repeat(3, max-content);
    align-items: center;
    grid-column-gap: $gap;
    padding-left: $gap;
    height: 60px;

    background-repeat  : no-repeat;
    background-position: center;
    background-size    : 100% 100%;
    background-image   : url("/static-p/img/admin-org-header/bg.png");

    .title {
        color: #eee;
        font-size: 18px;

    }

    .ivu-avatar {
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
    }

    .menu-user {
        .ivu-dropdown-item {
            padding: 0;

            a {
                display: block;
                padding: 7px 16px;
            }
        }
    }
}
</style>
