# ---- build stage ----
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---- self-signed certificate stage ----
FROM alpine:3.20 AS certs
RUN apk add --no-cache openssl \
    && mkdir -p /certs \
    && openssl req -x509 -nodes -newkey rsa:2048 -days 3650 \
        -keyout /certs/server.key \
        -out /certs/server.crt \
        -subj "/C=RU/ST=State/L=City/O=SchoolTracking/CN=localhost" \
        -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"

# ---- runtime stage ----
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=certs /certs/server.crt /etc/nginx/certs/server.crt
COPY --from=certs /certs/server.key /etc/nginx/certs/server.key
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80 443
CMD ["nginx", "-g", "daemon off;"]