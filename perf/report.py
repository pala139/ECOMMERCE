"""Produce a Markdown comparison from actual k6 summary-export JSON files."""

import json
import sys

def load(path):
    with open(path, encoding="utf-8") as file:
        return json.load(file)["metrics"]

uncached, cached = map(load, sys.argv[1:3])
print("## Catalog smoke benchmark")
print("Synthetic dataset: 100 categories, 1,000 products; 10 virtual users; 30 seconds per mode.")
print("")
print("| Metric | Redis disabled | Redis enabled |")
print("|---|---:|---:|")
for label, metric, key, unit in [
    ("Category p95", "category_latency", "p(95)", "ms"),
    ("Category median", "category_latency", "med", "ms"),
    ("Product p95", "product_latency", "p(95)", "ms"),
    ("HTTP throughput", "http_reqs", "rate", "req/s"),
]:
    print(f"| {label} | {uncached[metric][key]:.2f} {unit} | {cached[metric][key]:.2f} {unit} |")
print("\nShared CI runner, single API instance; results are not production capacity guarantees.")
