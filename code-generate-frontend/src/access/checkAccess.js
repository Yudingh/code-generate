import ACCESS_ENUM from './accessEnum'

const accessRank = {
  [ACCESS_ENUM.NOT_LOGIN]: 0,
  [ACCESS_ENUM.USER]: 1,
  [ACCESS_ENUM.ADMIN]: 2,
}

const checkAccess = (currentAccess = ACCESS_ENUM.NOT_LOGIN, needAccess = ACCESS_ENUM.NOT_LOGIN) => {
  return (accessRank[currentAccess] ?? 0) >= (accessRank[needAccess] ?? 0)
}

export default checkAccess
