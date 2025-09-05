local rateLimitKey = KEYS[1];
local rate = tonumber(ARGV[1]);
local rateInterval = tonumber(ARGV[2]);
local ttlResult = rateInterval;
local limitResult = 0;
local currValue = redis.call('incr', rateLimitKey);
if (currValue == 1) then
    redis.call('expire', rateLimitKey, rateInterval);
    limitResult = 0;
else
    if (currValue > rate) then
        limitResult = 1;
    end
    ttlResult = redis.call('ttl', rateLimitKey);
end
return { limitResult, ttlResult }