/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Franklin Covey Priority Colors
        priority: {
          a: '#ef4444', // Critical - Red
          b: '#f97316', // Important - Orange
          c: '#eab308', // Complete When Possible - Yellow
          d: '#22c55e', // Delegate/Defer - Green
        }
      }
    },
  },
  plugins: [],
}
