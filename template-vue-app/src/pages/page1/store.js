import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

export default new Vuex.Store({
    state: {
        user: {}, // 登录用户
    },
    mutations: {
        // 设置登录用户
        setUser(state, user) {
            state.user = user;
        }
    },
    actions: {

    },
    getters: {
        // 已登录返回 true，否则返回 false
        logined(state) {
            return state.user.id;
        }
    }
});
