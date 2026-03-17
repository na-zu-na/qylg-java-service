--获取最小
local res=redis.call('ZRANGE',KEY[1],0,0)

if #res==0 then
	return nil
end

local csId = res[1]

--负载+1
redis.call('ZINCRBY',KEYS[1],1)

return csId


local csId = redis.call('HGET', KEYS[2], ARGV[1])
if not csId then return 0 end
redis.call('ZINCRBY', KEYS[1], -1, csId);
redis.call('HDEL', KEYS[2], ARGV[1])
return 1