import Vue from 'vue';
import Vuex from 'vuex';
import UserDao from '@/../public/static-m/js/dao/UserDao';

Vue.use(Vuex);

export default new Vuex.Store({
    state: {
        user: {}, // 当前登录用户
    },
    modules: {
    },
    mutations: {
        // 设置登录用户
        setUser(state, user) {
            state.user = user;
        }
    },
    actions: {
        // 从服务器获取当前登录用户
        loadCurrentUser({ commit }) {
            return UserDao.findCurrentUser().then(user => {
                commit('setUser', user);
            });
        }
    },
    getters: {
        // 已登录返回 true，否则返回 false
        logined(state) {
            return !!state.user.id;
        }
    }
});
