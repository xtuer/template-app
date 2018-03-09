import 'iview/dist/styles/iview.css';
import Vue    from 'vue';
import iView  from 'iview';
import App    from './index.vue';
import router from './router';
import store  from './store';

Vue.use(iView);
Vue.config.productionTip = false;

new Vue({
    el: '#app',
    router: router,
    store: store,
    render: h => h(App)
});
