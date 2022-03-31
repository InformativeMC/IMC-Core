# IMC Core Documentation

## General Structure

All APIs are exposed under `https:<address>:<port>/api/`. The response of each API is in similar structure:

```typescript
interface ApiResoonse<DetailT> {
    requestStatus: string,
    requestInfo: string,
    requestDetail: DetailT | null,
}
```

Field `requestStatus` indicates if request successes. For a successful request, it will be `"success"`. Otherwise, it
will be `"error"`. The `requestInfo` shows error information. The "real" response content is in `requestDetail`.

## Heartbeat

### Method

**GET**

### Parameter

No parameter is needed.

### Response

```typescript
interface HeartbeatReseponseDetail {
    status: string,
}
```

- `status`: Usually `"healthy"`.

## IMC User Register

### Method

**POST**

### Post Form

```typescript
interface UserRegisterForm {
    username: string,
    token: string,
}
```

- `username`: The username to be registered.
- `token`: A temporary authentication token, which is obtained from Minecraft by command `/imc auth`. Only when
  the `token` is id, a register is accepted.

### Response

```typescript
interface UserRegisterResponseDetail {
    username: string,
    key: string,
}
```

- `key`: API authentication key for future usage.

## IMC User Test

### Method

**POST**

### Post Form

```typescript
interface UserTestForm {
    username: string,
    token: string,
}
```

- `token`: The user's API key, which is the same as the `key` obtained during register.

### Response

```typescript
interface UserTestResponseDetail {
    username: string,
}
```

## JVM Information

### Method

**POST**

### Post Form

```typescript
interface JvmInfoForm {
    username: string,
    token: string,
}
```

- `token`: The user's API key.

### Response

```typescript
interface JvmInfoResponseDetail {
    // Jvm Info
    jvmName: String,
    jvmVendor: String,
    jvmVersion: String,
    jvmInfo: String,
    // Java & Kotlin version
    javaVersion: String,
    kotlinVersion: String,
}
```

## OS Information

### Method

**POST**

### Post Form

```typescript
interface OSInfoForm {
    username: string,
    token: string,
}
```

- `token`: The user's API key.

### Response

```typescript
interface OSInfoResponseDetail {
    // OS Info
    osName: String,
    maxMemory: String,
    allocatedMemory: String,
    freeMemory: String,
}
```

## Player Statics

### Method

**POST**

### Post Form

```typescript
interface PlayerStatForm {
    username: string,
    token: string,
    target: Array<string>,
    operation: string,
    arg: Array<string>,
}
```

- `token`: The user's API key.
- `target`: The Minecraft users operation is acted on. Empty array indicates all users.
- `operation`: One of the followings:
    - `watch`: Watch user statics without modifying.
    - `damage`: Deal `arg[0]` amount damage on target.
    - `feed`: Feed target with `arg[0]` amount food and `arg[1]` saturation modifier.
    - `heal`: Heal target with `arg[0]` amount.

### Response

```typescript
interface SinglePlayerStat {
    name: string,
    entityName: string,
    uuid: string,
    health: number, // floating point number
    foodLevel: number, // integer number
    experienceLevel: number, // integer number
}

interface PlayerStatResponseDetail {
    players: Array<SinglePlayerStat>
}
```

## Give Inventory

### Method

**POST**

### Post Form

```typescript
interface GiveInventoryForm {
    username: string,
    token: string,
    target: Array<string>,
    itemId: string,
    count: number, // integer number
}
```

- `token`: The user's API key.
- `target`: Minecraft players affected. The same as **PlayerStat** section.
- `itemId`: Minecraft item identifier.
- `count`: Number of items give to Minecraft players.

### Response

Empty detail.

## Game Message

### Method

**POST**

### Post Form

```typescript
interface GameMessageForm {
    username: string,
    token: string,
    target: Array<string>,
    message: string,
    actionBar: boolean,
}
```

- `token`: The user's API key.
- `target`: Minecraft players affected. The same as **PlayerStat** section.
- `message`: Message to be sent. There are some placeholders. See below.
- `actionBar`: If the message is shown in chat HUD or action bar.

Possible placeholders for message:

- `%[date]%`: Will be replaced with current date.
- `%[time]%`: Will be replaced with current time.
- `%[username]%`: Will be replaced with the username message is sent to.

### Response

Empty Detail.