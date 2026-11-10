Problem and non-goals. What the service does, and what it deliberately doesn’t (no user accounts, no dashboard UI, one provider format).

How is a duplicate recognised? Which field identifies an event, and what enforces uniqueness?

When do you answer the provider? Before or after forwarding the event to the application, and why?

What is the retry policy? How many attempts, what delays, and what happens after the last one?

What if the service crashes halfway through a delivery? How does it avoid losing the event or sending it twice?

What about events that arrive out of order?
