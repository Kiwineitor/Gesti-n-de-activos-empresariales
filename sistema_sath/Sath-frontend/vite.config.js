import {fileURLToPath, URL} from 'node:url'
import {defineConfig, loadEnv} from 'vite'
import vue from '@vitejs/plugin-vue'
// import vueDevTools from 'vite-plugin-vue-devtools'
import path from 'node:path'

export default defineConfig(({mode}) => {
    const env = loadEnv(mode, process.cwd(), '')
    const BACKEND = env.BACKEND_URL || 'http://localhost:9006'

    return {
        base: '/',

        plugins: [
            vue(),
            // vueDevTools({ launchEditor: 'idea' }),
        ],

        resolve: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url)),
            },
        },

        server: {
            host: '0.0.0.0',
            port: parseInt(env.VITE_PORT || '5173'),
            proxy: {
                '/api': {
                    target: BACKEND,
                    changeOrigin: true
                },
                '/ws': {
                    target: BACKEND,
                    changeOrigin: true,
                    ws: true
                },
            },
        },

        build: {
            outDir: path.resolve(
                __dirname,
                '../src/main/resources/static'
            ),
            emptyOutDir: true
        }
    }
})