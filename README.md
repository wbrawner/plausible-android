# Plausible Android

This is an **unofficial** Android SDK to record events with a [Plausible] backend.

## Usage

### Configuration

For simple use cases, you can just declare the domain for which you'd like to send events in your `strings.xml` file:

```xml
<string name="plausible_domain">example.com</string>
```

If you're self-hosting Plausible, you'll need to provide the URL for your instance as well:

```xml
<string name="plausible_host">https://plausible.my-company.com</string>
```

By default, the SDK will be enabled at app startup, though you can prevent this to allow users to
opt-in or opt-out like so:

```xml
<string name="plausible_enable_startup">true</string>
```

You can then manually enable the sdk with the following:

```java
Plausible.enable(true)
```

If you'd like to set a static user agent, you can do that as well:

```xml
<string name="plausible_user_agent" />
```

Though it's probably best to use a unique user-agent for 

### Sending Events

#### Page Views

```java
Plausible.pageView("/settings")
```

#### Custom Events

```java
Plausible.event("ctaClick")
```

## Download

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.wbrawner.plausble:plausible-android:0.1.0-SNAPSHOT'
}
```

[Plausible]: https://plausible.io