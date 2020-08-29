<!-- 页面布局 -->
<template>
    <div class="home">
        <!-- Header -->
        <Header>Magic 管理系统</Header>

        <div class="main">
            <!-- 左侧侧边栏 -->
            <div class="sidebar">
                <Menu :active-name="activeName" :open-names="['1']" width="auto" @on-select="navigateTo">
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
            menuItems: [ // 所有菜单项，每个菜单项有不同的权限
                { label: '问题管理', name: 'about'               },
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
    },
    watch: {
        // 监听路由变化时高亮对应的菜单项
        $route(to, from) {
            if (this.menuItems.some(item => item.name === to.name)) {
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
