// import parseTime, formatTime and set to filter

export { parseTime, formatTime } from '@/utils'
/**
 * Show plural label if time is plural number
 * @param {number} time
 * @param {string} label
 * @return {string}
 */
function pluralize(time, label) {
  if (time === 1) {
    return time + label
  }
  return time + label + 's'
}
/**
 * @param {number} time
 */
export function timeAgo(time) {
  const between = Date.now() / 1000 - Number(time)
  if (between < 3600) {
    return pluralize(~~(between / 60), ' minute')
  } else if (between < 86400) {
    return pluralize(~~(between / 3600), ' hour')
  } else {
    return pluralize(~~(between / 86400), ' day')
  }
}

/**
 * Number formatting
 * like 10000 => 10k
 * @param {number} num
 * @param {number} digits
 */
export function numberFormatter(num, digits) {
  const si = [
    { value: 1E18, symbol: 'E' },
    { value: 1E15, symbol: 'P' },
    { value: 1E12, symbol: 'T' },
    { value: 1E9, symbol: 'G' },
    { value: 1E6, symbol: 'M' },
    { value: 1E3, symbol: 'k' }
  ]
  for (let i = 0; i < si.length; i++) {
    if (num >= si[i].value) {
      return (num / si[i].value).toFixed(digits).replace(/\.0+$|(\.[0-9]*[1-9])0+$/, '$1') + si[i].symbol
    }
  }
  return num.toString()
}

/**
 * 10000 => "10,000"
 * @param {number} num
 */
export function toThousandFilter(num) {
  return (+num || 0).toString().replace(/^-?\d+/g, m => m.replace(/(?=(?!\b)(\d{3})+$)/g, ','))
}

/**
 * Upper case first char
 * @param {String} string
 */
export function uppercaseFirst(string) {
  return string.charAt(0).toUpperCase() + string.slice(1)
}

/**
 * 通用状态过滤器
 * @param value
 * @returns {*}
 */
export function stateFilter(value) {
  const map = {
    '0': '正常',
    '1': '禁用'
  }
  return map[value]
}

/**
 * 是否类型过滤器
 * @param value
 * @returns {*}
 */
export function booleanFilter(value) {
  const map = {
    'true': '是',
    'false': '否',
    '0': '否',
    '1': '是'
  }
  return map[value]
}
/**
 * 百分比过滤器
 * @param value
 * @returns {*}
 */
export function percentFilter(value) {
  return (value * 100) + '%'
}

export function paperStateFilter(value) {
  const map = {

  }
  return map[value]
}

export function quLevelFilter(value) {
  const map = {
    '1': '普通',
    '2': '较难'
  }
  return map[value]
}

export function menuTypeFilter(value) {
  const map = {
    '1': '菜单',
    '2': '功能',
    '3': '路由'
  }
  return map[value]
}

