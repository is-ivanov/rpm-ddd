# Expected Load

- Target: 20,000 patients within 2 years
- Each patient submits ~2 vital readings per day via IoT devices
- Daily ingestion: ~40,000 readings/day; annual: ~14.6M readings/year
- Single agency, multiple hospitals/physician groups (shared DB, org-level isolation)
- 10-50 concurrent staff and physicians during peak hours
- IoT data ingestion is asynchronous — no strict real-time latency requirement
- Historical data retention per regulatory requirements
