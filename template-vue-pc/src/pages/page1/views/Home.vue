<!-- 页面布局 -->
<template>
    <div class="home">
        <!-- Header -->
        <Header>Magic 管理系统</Header>

        <div class="main">
            <!-- 左侧侧边栏 -->
            <div class="sidebar">
                <Menu :active-name="activeName" :open-names="['about']" width="auto" @on-select="navigateTo">
                    <MenuItem v-for="item in menuItems" :key="item.name" :name="item.name">{{ item.label }}</MenuItem>
                </Menu>
            </div>

            <!-- 内容显示区 -->
            <div class="content">
                <Scroll>
                    <div class="content-wrapper">
                        <router-view/>
                    </div>
                </Scroll>
            </div>
        </div>
    </div>
</template>

<script>
import Header from '@/components/Header.vue';

export default {
    components: {
        Header,
    },
    data() {
        return {
            activeName: '',
            // 所有菜单项，每个菜单项有不同的权限
            menuItems: [
                // { label: '机构管理', name: 'orgs', roles: ['ROLE_ADMIN_SYSTEM'] },
                { label: '问题管理', name: 'about',    roles: [] },
                { label: '任务管理', name: 'scroll'              },
                { label: '课程处理', name: 'admin-courses'       },
                { label: '问题统计', name: 'question-statistics' },
                { label: '绩效查询', name: 'performance',        },
                { label: '问题类型', name: 'question-types',     },
                { label: '用户管理', name: 'admin-users',        },
            ],
        };
    },
    mounted() {
        this.activeName = this.$route.name;
    },
    methods: {
        navigateTo(name) {
            this.$router.push({ name });
        },
        // 判断用户是否有访问 routeName 的权限
        hasPermission(routeName) {
            /*
             逻辑:
             1. 获取当前正在访问的路由对应的菜单项
             2. 如果不存在，或者菜单项无权限要求，则此菜单项则允许访问
             3. 当前用户的权限中包含了菜单项需要的某一个权限则有权访问此菜单项，否则无权访问
             */

            // [1] 获取当前正在访问的路由对应的菜单项，如果不存在对应的菜单项则允许访问
            const menuItem = this.menuItemsMap.get(routeName);

            // [2] 如果不存在，或者菜单项无权限要求，则此菜单项则允许访问
            if (!menuItem) {
                return true;
            }
            if (!menuItem.roles || menuItem.roles.length === 0) {
                return true;
            }

            for (let role of menuItem.roles) {
                // [3] 当前用户的权限中包含了菜单项需要的某一个权限则有权访问此菜单项，否则无权访问
                if (this.userRoles.includes(role)) {
                    return true;
                }
            }

            return false;
        },
    },
    computed: {
        // 当前用户有权使用的菜单项
        myMenuItems() {
            /*
             逻辑:
             1. 遍历所有菜单项，用户有任何菜单项的权限，就有权访问这个菜单项
             2. 对得到的菜单项根据 order 进行升序排序
             */
            const items = [];

            // [1] 遍历所有菜单项，用户有任何菜单项的权限，就有权访问这个菜单项
            this.menuItemsMap.forEach(item => {
                if (this.hasPermission(item.name)) {
                    items.push(item);
                }
            });

            // [2] 对得到的菜单项根据 order 进行升序排序
            items.sort((a, b) => a.order - b.order);

            return items;
        },
        // 菜单项创建的 Map，加速查询
        menuItemsMap() {
            const map = new Map();

            this.menuItems.forEach((item, index) => {
                map.set(item.name, { ...item, order: index });
            });

            return map;
        },
        // 当前用户的权限
        userRoles() {
            return this.$store.state.user.roles || [];
        }
    },
    watch: {
        // 监听路由变化时检查是否有权限访问，以及高亮对应的菜单项
        $route(to, from) {
            // 无权限访问者跳转到 404
            if (!this.hasPermission(to.name)) {
                this.$router.replace({ path: '404' });
            }

            // 高亮菜单项
            if (this.menuItemsMap.get(to.name)) {
                this.activeName = to.name;
            }
        }
    }
};
</script>

<style lang="scss">
.home {
    display: flex;
    flex-direction: column;
    width : 100vw;
    height: 100vh;

    > .header {
        box-shadow: 0 0px 15px #ccc;
        z-index: 1000;
        height: 60px;
    }

    > .main {
        display: flex;
        flex: 1;

        > .sidebar {
            width: 180px;

            // 隐藏 Menu 右边框
            .ivu-menu-vertical.ivu-menu-light::after {
                display: none;
            }

            .ivu-menu-light.ivu-menu-vertical .ivu-menu-item-active:not(.ivu-menu-submenu):after {
                background: #5cadff;
            }
        }

        > .content {
            flex: 1;
            background: #eceef8;
            padding: 24px;

            > .content-wrapper, > .__vuescroll > .__panel > .__view > .content-wrapper {
                padding: 18px;
                background: white;
                border-radius: 4px;
                overflow: auto;
                min-height: calc(100vh - 60px - 50px);
            }
        }
    }
}
</style>
