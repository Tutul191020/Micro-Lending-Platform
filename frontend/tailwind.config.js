/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                primary: {
                    DEFAULT: '#059669', // Emerald-600
                    hover: '#047857',
                    light: '#D1FAE5',
                },
                secondary: {
                    DEFAULT: '#0F172A', // Slate-900
                    light: '#1E293B',
                },
                accent: {
                    DEFAULT: '#F97316', // Coral-500
                    hover: '#EA580C',
                },
                background: '#F3F4F6', // Gray-100
                surface: '#FFFFFF',
                auth: {
                    purple: '#6B72D6',
                    cream: '#FFF0E3',
                    teal: '#8EDDD6',
                    'teal-btn': '#7AC7C4',
                }
            },
            borderRadius: {
                DEFAULT: '0.75rem', // 12px (rounded-xl)
                'button': '9999px', // rounded-full
            },
            fontFamily: {
                sans: ['Inter', 'Plus Jakarta Sans', 'sans-serif'],
            },
        },
    },
    plugins: [],
}
