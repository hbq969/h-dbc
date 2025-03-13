const {defineConfig} = require('@vue/cli-service')
const path = require('path')
module.exports = defineConfig({
    transpileDependencies: true,
    outputDir: 'dbc-ui',
    publicPath: './',
    devServer: {
        proxy: {
            '/dev': {
                target: `http://localhost:30170`,
                changeOrigin: true,
                secure: false,
                pathRewrite: {'^/dev': ''},
            },
        },
    },
    configureWebpack: {
        resolve: {
            alias: {
                '@': path.resolve(__dirname, 'src')
            }
        }
    }
})
