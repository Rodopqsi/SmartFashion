FROM node:18-alpine AS build
WORKDIR /app
COPY ../cliente/frontend/package*.json ./
RUN npm ci
COPY ../cliente/frontend/ .
ARG VITE_API_BASE
ARG VITE_GOOGLE_CLIENT_ID
ENV VITE_API_BASE=${VITE_API_BASE}
ENV VITE_GOOGLE_CLIENT_ID=${VITE_GOOGLE_CLIENT_ID}
RUN npm run build

FROM nginx:stable-alpine
COPY --from=build /app/dist/ /usr/share/nginx/html/
# Also include any static /img folder from the source (some images live in /cliente/frontend/img)
COPY --from=build /app/img/ /usr/share/nginx/html/img/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
