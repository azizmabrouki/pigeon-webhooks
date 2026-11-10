# Pigeon: design note

## 1. Problem and scope

Payment providers notify an application by sending it events (webhooks).
Events can arrive twice, arrive out of order, or arrive while the
application is down. Pigeon sits between the provider and the application:
it verifies each event, stores it, and delivers it reliably.

### Goals
1. Verify the signature of every incoming event and reject forged ones.
2. Store each event once, even if the provider sends it several times.
3. Deliver stored events to the application, retrying when it is down.
4. Park events that keep failing, so an operator can inspect and replay them.
5. Expose an API to list events and their delivery status.

### Stretch goals (only after the core is tested and deployed)
1. Read-only dashboard.
2. A second provider format.
3. User accounts with roles.

### Non-goals
- Guaranteed ordering of events.
- Checking events against the provider's own API.

## 2. Recognising a duplicate

Each event carries the provider's event ID. The events table has a
uniqueness rule on (provider, event ID). Pigeon inserts the event and lets
the database reject a duplicate; it does not check first and insert after.

Why: two copies arriving at the same instant would both pass a check, but
only one can pass the insert. On a duplicate, Pigeon answers 200 so the
provider stops resending.

## 3. When Pigeon answers the provider

Order of steps: verify the signature, store the event, answer 200, then
deliver in the background.

Why: providers wait only a few seconds before resending. If Pigeon waited
for the application, an outage there would become timeouts and duplicate
floods from the provider. Once the event is stored, Pigeon has taken
responsibility for it, so it is safe to answer.

A bad signature gets a 4xx answer and nothing is stored.

## 4. Retry policy

- Delays grow after each failed attempt: 1 min, 5 min, 30 min, 2 h, 6 h.
- After 5 failed attempts the event is marked failed and waits for a
  manual replay.
- Retried: timeouts, connection errors, 5xx answers, 429.
- Not retried: other 4xx answers. The application rejected the content,
  and sending it again will not change that.

Why growing delays: an application that is down needs time to recover, and
frequent retries add load exactly when it can least handle it.

## 5. Crash during delivery

The database is the only state. Each event has a status: RECEIVED,
DELIVERED or FAILED, plus an attempt count and the time of the next
attempt. On startup, Pigeon resumes every event that is not DELIVERED or
FAILED.

Known limit: if Pigeon crashes after the application received an event but
before recording DELIVERED, the event is sent again on restart. Pigeon
therefore guarantees: stored once, delivered at least once. Every delivery
carries the event ID in a header so the application can ignore a repeat.

## 6. Events out of order

Pigeon does not reorder or drop events. It forwards each one with its
original creation time from the provider, and the application decides how
to treat a late event.

Why: dropping a late event could lose a real payment, and correct
reordering needs knowledge of the business rules that only the application
has.