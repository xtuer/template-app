process.env.VUE_APP_VERSION = new Date().getTime();

module.exports = {
    devServer: {
        port: 8888,
        proxy: 'http://localhost:8080'
    },

    // 多页的页面
    pages: {
        page1: 'src/pages/page1/main.js',
        page2: 'src/pages/page2/main.js',
    },

    // yarn build 的输出目录
    outputDir: '../template-web-gradle/src/main/webapp/WEB-INF/page-vue',
    assetsDir: 'static',

    css: {
        loaderOptions: {
            sass: {
                data: `
                    @import "@/../public/static/css/variables.scss";
                `
            }
        }
    }
};
