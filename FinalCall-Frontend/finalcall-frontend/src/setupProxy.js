// src/setupProxy.js
const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  // Proxy OAuth2 and Authentication API
  app.use(
    '/oauth2',
    createProxyMiddleware({
      target: 'http://localhost:8081',
      changeOrigin: true,
      ws: true,
    })
  );

  app.use(
    '/api',
    createProxyMiddleware({
      target: 'http://localhost:8081',
      changeOrigin: true,
      ws: true,
    })
  );

  // Proxy CatalogueService (if needed with a path rewrite)
  app.use(
    '/catalogue-api',
    createProxyMiddleware({
      target: 'http://localhost:8082',
      changeOrigin: true,
      pathRewrite: { '^/catalogue-api': '/api' },
      ws: true,
    })
  );

  // Proxy PaymentService (if needed with a path rewrite)
  app.use(
    '/payment-api',
    createProxyMiddleware({
      target: 'http://localhost:8083',
      changeOrigin: true,
      pathRewrite: { '^/payment-api': '/api' },
      ws: true,
    })
  );

  // Proxy AuctionService (if needed with a path rewrite)
  app.use(
    '/auction-api',
    createProxyMiddleware({
      target: 'http://localhost:8084',
      changeOrigin: true,
      pathRewrite: { '^/auction-api': '/api' },
      ws: true,
    })
  );

  // **Important**: Proxy WebSocket connections to AuctionService
  app.use(
    '/ws',
    createProxyMiddleware({
      target: 'ws://localhost:8084',
      ws: true,
      changeOrigin: true,
    })
  );
};
