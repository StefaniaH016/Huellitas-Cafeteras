# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

COPY huellitas-frontend/package*.json ./

RUN npm ci

COPY huellitas-frontend .

RUN npm run build

# Stage 2: Runtime
FROM nginx:alpine

COPY --from=builder /app/dist/huellitas-frontend/browser /usr/share/nginx/html
COPY huellitas-frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
