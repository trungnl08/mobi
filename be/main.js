const jsonServer = require('json-server');
const server = jsonServer.create();
const router = jsonServer.router('db.json');
const middlewares = jsonServer.defaults();
const queryString = require('query-string');
const fs = require('fs');
const { exec } = require('child_process');

// Set default middlewares (logger, static, cors and no-cache)
server.use(middlewares);

// Add custom routes before JSON Server router
server.get('/echo', (req, res) => {
  res.jsonp(req.query);
});

server.use(jsonServer.bodyParser);
server.use((req, res, next) => {
  if (req.method === 'POST') {
    req.body.createdAt = Date.now();
    req.body.updatedAt = Date.now();
  } else if (req.method === 'PATCH') {
    req.body.updatedAt = Date.now();
  }
  next();
});

router.render = (req, res) => {
  const headers = res.getHeaders();
  const totalCountHeader = headers['x-total-count'];
  if (req.method == 'GET' && totalCountHeader) {
    const queryParams = queryString.parse(req._parsedUrl.query);
    const result = {
      data: res.locals.data,
      pagination: {
        _page: Number.parseInt(queryParams._page) || 1,
        _limit: Number.parseInt(queryParams._limit) || 10,
        _totalRow: Number.parseInt(totalCountHeader),
      },
    };
    console.log('1l an');
    return res.jsonp(result);
  }
  //otherwise, keep default bahavior
  if (req.method == 'HEAD') {
    console.log('123');
    fs.unlink('./db.json', (err) => {
      if (err) {
        console.log('failed');
        throw err;
      }
      console.log('deleted');
      exec('killall node && npm start', (error, stdout, stderr) => {
        if (error) {
          console.error(`exec error: ${error}`);
          return;
        }
        console.log('okela')
      });
      
    });
    return res.jsonp([]);
  }
  res.jsonp(res.locals.data);
};

// Use default router
const PORT = 4000;
server.use('/api', router);
server.listen(PORT, () => {
  console.log('JSON Server is running');
});
