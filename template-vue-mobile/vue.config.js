process.env.VUE_APP_VERSION = new Date().getTime();

const CompressionWebpackPlugin = require('compression-webpack-plugin');

module.exports = {
    devServer: {
        port: 8888,
        proxy: 'http://localhost:8080',
        disableHostCheck: true,
    },

    // yarn build 的输出目录
    outputDir: '../template-web-boot/src/main/resources/page-m',
    assetsDir: 'static-m',

    productionSourceMap: false, // 不生成 map 文件
    configureWebpack: config => {
        if (process.env.NODE_ENV === 'production') {
            return {
                plugins: [new CompressionWebpackPlugin({
                    test: /\.(js|css)(\?.*)?$/i, // 需要压缩的文件正则
                    threshold: 10240,            // 文件大小大于这个值时启用压缩 (10K)
                    deleteOriginalAssets: false, // 压缩后保留原文件
                })]
            };
        }
    },
};
