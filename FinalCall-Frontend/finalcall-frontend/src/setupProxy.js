// setupProxy.js
const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  // Authentication Service Proxy
  app.use(
    ['/oauth2', '/api/auth', '/api/users'],
    createProxyMiddleware({
      target: 'http://localhost:8081',
      changeOrigin: true,
      ws: true,
    })
  );

  // Catalogue Service Proxy
  app.use(
    ['/api/items', '/ws', '/itemimages'],
    createProxyMiddleware({
      target: 'http://localhost:8082',
      changeOrigin: true,
      ws: true,
    })
  );

  // Auction Service Proxy
  app.use(
    ['/api/auctions', '/ws/auctions', '/ws/notifications'],
    createProxyMiddleware({
      target: 'http://localhost:8084',
      changeOrigin: true,
      ws: true,
    })
  );

  // Payment Service Proxy
  app.use(
    '/api/payments',
    createProxyMiddleware({
      target: 'http://localhost:8083',
      changeOrigin: true,
      ws: true,
    })
  );
};