-- KEYS[1]: 延迟队列 ZSet
-- KEYS[2]: retryCount key前缀

-- ARGV[1]: 当前时间戳
-- ARGV[2]: 每次最多取多少条

local zsetKey = KEYS[1]
local countPrefix = KEYS[2]

local now = tonumber(ARGV[1])
local limit = tonumber(ARGV[2])

-- 1. 取到期任务
local entries = redis.call('ZRANGEBYSCORE', zsetKey, 0, now, 'LIMIT', 0, limit)

if (#entries == 0) then
	return {}
end

local result = {}

for i, messageId in ipairs(entries) do

-- 2. 删除任务（防止重复消费）
	redis.call('ZREM', zsetKey, messageId)

	-- 3. 获取重试次数
	local retryKey = countPrefix .. messageId
	local retryCount = redis.call('GET', retryKey)

	if (not retryCount) then
		retryCount = "0"
	end

	table.insert(result, messageId)
	table.insert(result, retryCount)
end

return result