import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const categoryLatency = new Trend('category_latency', true);
const productLatency = new Trend('product_latency', true);

export const options = {
  vus: 10,
  duration: '30s',
  summaryTrendStats: ['avg', 'med', 'p(95)', 'p(99)', 'max'],
  thresholds: { http_req_failed: ['rate<0.01'], checks: ['rate>0.99'] },
};

const base = __ENV.BASE_URL || 'http://localhost:8080';
export default function () {
  const products = http.get(base + '/products?page=0&size=20&sort=id');
  productLatency.add(products.timings.duration);
  check(products, { 'product page returns 200': r => r.status === 200 });
  const categories = http.get(base + '/categories');
  categoryLatency.add(categories.timings.duration);
  check(categories, { 'categories return 200': r => r.status === 200 });
}
