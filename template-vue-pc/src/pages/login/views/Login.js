import OrgDao from '@/../public/static-p/js/dao/OrgDao';
import UserDao from '@/../public/static-p/js/dao/UserDao';

export default {
    data() {
        return {
            orgs    : [], // 所有机构
            orgId   : '', // 选中的机构 ID
            username: '', // 登录账号
            password: '', // 登录密码
        };
    },
    mounted() {
        // 获取机构
        OrgDao.findOrganizations({ pageNumber: 1, pageSize: 1000 }).then(orgs => {
            // 插入管理系统
            orgs.unshift({
                orgId: '1',
                name: '管理系统',
            });

            this.orgId = '1';
            this.orgs  = orgs;
        });
    },
    methods: {
        login() {
            /*
             逻辑:
             1. 登录
             2. 登录成功后获取用户信息
             3. 用户权限包含 ROLE_ADMIN_SYSTEM 则跳转到机构管理页
             4. 普通用户跳转到文件管理页
             */

            // [1] 登录
            UserDao.login(this.username, this.password, this.orgId).then(() => {
                // [2] 登录成功后获取用户信息
                return UserDao.findCurrentUser();
            }).then(user => {
                if (user.roles.includes('ROLE_ADMIN_SYSTEM')) {
                    // [3] 用户权限包含 ROLE_ADMIN_SYSTEM 则跳转到机构管理页
                    window.location.href = '/admin#/orgs';
                } else {
                    // [4] 普通用户跳转到文件管理页
                    window.location.href = '/admin#/org-files';
                }
            });
        }
    }
};
