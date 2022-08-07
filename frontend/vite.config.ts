import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: [{
      find: 'stompjs',
      replacement: './node_modules/stompjs/lib/stomp.js',
    }],
  },
  server: {
    proxy: {
      '/api': {
        target: 'ws://localhost:8080',
        ws: true,
        secure: false,
      },
    },
  },
})
