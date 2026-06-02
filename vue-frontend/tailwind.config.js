/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx,vue}',
  ],
  theme: {
    extend: {
      colors: {
        primary: { DEFAULT: '#2563eb', dark: '#1d4ed8', light: '#eff6ff' },
        orange: { DEFAULT: '#f97316', dark: '#ea580c' },
        sidebar: { bg: '#0f172a', hover: '#1e293b' }
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'Noto Sans SC', 'Microsoft YaHei', 'sans-serif'],
        mono: ['Cascadia Code', 'Fira Code', 'Consolas', 'monospace']
      },
      borderRadius: {
        xs: '6px',
        sm: '8px',
        DEFAULT: '12px'
      }
    }
  },
  plugins: []
}
