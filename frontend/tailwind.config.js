/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Custom color palette matching the SaaS dashboard theme
        background: '#0f172a', // Deep Slate
        surface: 'rgba(255, 255, 255, 0.05)',
      }
    },
  },
  plugins: [],
}
