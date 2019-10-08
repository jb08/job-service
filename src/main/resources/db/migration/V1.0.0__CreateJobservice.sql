CREATE TABLE "demo"."jobs" (
  "id"          SERIAL PRIMARY KEY,
  "total"       numeric not null check (total >= 0),
  "progress"    numeric not null,
  updated_at    timestamp not null default now()
);
