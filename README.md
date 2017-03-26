# Firebase
Firebase 데이터 저장, 검색을 이용한 채팅 만들기

##Android에서 데이터 저장

Firebase 실시간 데이터베이스에 데이터를 쓰는 메소드는 다음과 같이 4가지입니다.

**setValue** 
> 정의된 경로(예: users/<user-id>/<username>)에 데이터를 쓰거나 대체합니다.

**push()** 
> 데이터 목록에 추가합니다. push()를 호출할 때마다 Firebase에서 고유 식별자로도 사용할 수 있는 고유 키(예: user-posts/<user-id>/<unique-post-id>)를 생성합니다.

**updateChildren()**
> 정의된 경로에서 모든 데이터를 대체하지 않고 일부 키를 업데이트합니다.

**runTransaction()**
> 동시 업데이트에 의해 손상될 수 있는 복잡한 데이터를 업데이트합니다.

##DatabaseReference 가져오기
데이터베이스에 데이터를 쓰려면 DatabaseReference의 인스턴스가 필요합니다.

```java
private DatabaseReference mDatabase;
// ...
mDatabase = FirebaseDatabase.getInstance().getReference();
```

##참조 위치에서 데이터 쓰기, 업데이트 또는 삭제
###기본 쓰기 작업

기본 쓰기 작업의 경우 setValue()를 사용하여 지정된 참조에 데이터를 저장하고 기존 경로의 모든 데이터를 대체할 수 있습니다. 이 메소드의 용도는 다음과 같습니다

	- 사용 가능한 JSON 유형에 해당하는 다음과 같은 유형을 전달합니다.
		- String
		- Long
		- Double
		- Boolean
		- Map<String, Object>
		- List<Object>
		
	맞춤 Java 개체를 전달합니다. 개체를 정의하는 클래스에는 인수를 취하지 않는 기본 생성자 및 지정할 속성에 대한 공개 getter가 있어야 합니다.
 

Java 개체를 사용하는 경우 개체의 내용이 하위 위치에 중첩된 형태로 자동으로 매핑됩니다. 또한 Java 개체를 사용하면 일반적으로 코드가 단순해지고 관리하기도 쉬워집니다. 예를 들어 기본적인 사용자 프로필이 있는 앱에서 User 개체는 다음과 비슷할 수 있습니다.


```java
IgnoreExtraProperties
public class User {

    public String username;
    public String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

}
```

**setValue()**로 다음과 같이 사용자를 추가할 수 있습니다.

```java
private void writeNewUser(String userId, String name, String email) {
    User user = new User(name, email);

    mDatabase.child("users").child(userId).setValue(user);
}
```

이 방법으로 setValue()를 사용하면 지정된 위치에서 하위 노드를 포함하여 모든 데이터를 덮어씁니다. 그러나 전체 개체를 다시 쓰지 않고도 하위 항목을 업데이트하는 방법이 있습니다. 사용자가 프로필을 업데이트하도록 허용하려면 다음과 같이 사용자 이름을 업데이트할 수 있습니다.
> mDatabase.child("users").child(userId).child("username").setValue(name);

###데이터 목록에 추가

멀티 사용자 애플리케이션에서 push() 메소드를 사용하여 목록에 데이터를 추가합니다. push() 메소드는 지정된 Firebase 참조에 새 하위 항목이 추가될 때마다 고유 키를 생성합니다. 목록의 새 요소마다 이러한 자동 생성 키를 사용하면 여러 클라이언트에서 쓰기 충돌 없이 동시에 같은 위치에 하위 항목을 추가할 수 있습니다. push()가 생성하는 고유 키는 타임스탬프에 기초하므로 목록 항목은 시간순으로 자동 정렬됩니다.

push() 메소드가 반환하는 새 데이터에 대한 참조를 사용하여 하위 항목의 자동 생성 키 값을 가져오거나 하위 데이터를 설정할 수 있습니다. push() 참조에 대해 getKey()를 호출하면 자동 생성 키 값이 반환됩니다.

이러한 자동 생성 키를 사용하여 데이터 구조를 평면화하는 작업을 단순화할 수 있습니다. 자세한 내용은 데이터 팬아웃 예제를 참조하세요.

###특정 필드 업데이트

다른 하위 노드를 덮어쓰지 않고 특정 하위 노드에 동시에 쓰려면 updateChildren() 메소드를 사용합니다.

updateChildren()을 호출할 때 키의 경로를 지정하여 하위 수준 값을 업데이트할 수 있습니다. 확장성을 위해 여러 위치에 데이터가 저장된 경우 데이터 팬아웃을 사용하여 해당 데이터의 모든 인스턴스를 업데이트할 수 있습니다. 예를 들어 소셜 블로깅 앱은 다음과 같은 Post 클래스를 사용할 수 있습니다.

```java
@IgnoreExtraProperties
public class Post {

    public String uid;
    public String author;
    public String title;
    public String body;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author, String title, String body) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("starCount", starCount);
        result.put("stars", stars);

        return result;
    }

}
```

블로깅 애플리케이션에서 게시물을 만든 후 최근 활동 피드 및 게시자의 활동 피드에 동시에 업데이트하려는 경우 다음과 같은 코드를 사용합니다.

```java
private void writeNewPost(String userId, String username, String title, String body) {
    // Create new post at /user-posts/$userid/$postid and at
    // /posts/$postid simultaneously
    String key = mDatabase.child("posts").push().getKey();
    Post post = new Post(userId, username, title, body);
    Map<String, Object> postValues = post.toMap();

    Map<String, Object> childUpdates = new HashMap<>();
    childUpdates.put("/posts/" + key, postValues);
    childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

    mDatabase.updateChildren(childUpdates);
}
```

이 예제에서는 push()를 사용하여 모든 사용자의 게시물을 포함하는 노드(/posts/$postid)에 게시물을 만드는 동시에 getKey()로 키를 검색합니다. 그런 다음 이 키로 사용자의 게시물 목록 (/user-posts/$userid/$postid)에 두 번째 항목을 만듭니다.

이러한 경로를 사용하면 이 예제에서 두 위치에 새 게시물을 만드는 방법과 같이 updateChildren()을 한 번만 호출하여 JSON 트리의 여러 위치에 동시에 업데이트할 수 있습니다. 이러한 동시 업데이트는 유기적 종속성을 갖습니다. 즉, 모든 업데이트가 한꺼번에 성공하거나 실패합니다.

###데이터 삭제

데이터를 삭제하는 가장 간단한 방법은 해당 데이터 위치의 참조에 대해 removeValue()를 호출하는 것입니다.

setValue() 또는 updateChildren() 등의 다른 쓰기 작업에 대한 값으로 null을 지정하여 삭제할 수도 있습니다. updateChildren()에 이 방법을 사용하면 API를 한 번 호출하여 여러 하위 항목을 삭제할 수 있습니다.

###완료 콜백 수신

Firebase 실시간 데이터베이스 서버에 데이터가 커밋되는 시점을 확인하려면 완료 리스너를 추가합니다. setValue()와 updateChildren()은 선택사항으로 완료 리스너를 취하며, 쓰기가 데이터베이스에 커밋되면 이 리스너가 호출됩니다. 이유를 불문하고 호출이 실패하면 실패 이유를 나타내는 오류 개체가 리스너로 전달됩니다.

###데이터를 트랜잭션으로 저장

증가 카운터와 같이 동시 수정으로 인해 손상될 수 있는 데이터를 다루는 경우 트랜잭션 작업을 사용할 수 있습니다. 이 작업에 지정하는 두 인수는 업데이트 함수 및 선택적 완료 콜백입니다. 업데이트 함수는 데이터의 현재 상태를 인수로 취하고 이 데이터에 새로 기록하려는 값을 반환합니다. 새 값이 기록되기 전에 다른 클라이언트에서 해당 위치에 기록하면 업데이트 함수가 새로운 현재 값으로 다시 호출되고 쓰기가 다시 시도됩니다.

예를 들어 소셜 블로깅 앱에서는 다음과 같이 사용자가 게시물에 별표를 주거나 삭제할 수 있게 하고 게시물이 받은 별표 수를 집계할 수 있습니다.

```java
private void onStarClicked(DatabaseReference postRef) {
    postRef.runTransaction(new Transaction.Handler() {
        @Override
        public Transaction.Result doTransaction(MutableData mutableData) {
            Post p = mutableData.getValue(Post.class);
            if (p == null) {
                return Transaction.success(mutableData);
            }

            if (p.stars.containsKey(getUid())) {
                // Unstar the post and remove self from stars
                p.starCount = p.starCount - 1;
                p.stars.remove(getUid());
            } else {
                // Star the post and add self to stars
                p.starCount = p.starCount + 1;
                p.stars.put(getUid(), true);
            }

            // Set value and report transaction success
            mutableData.setValue(p);
            return Transaction.success(mutableData);
        }

        @Override
        public void onComplete(DatabaseError databaseError, boolean b,
                               DataSnapshot dataSnapshot) {
            // Transaction completed
            Log.d(TAG, "postTransaction:onComplete:" + databaseError);
        }
    });
}
```

트랜잭션을 사용하면 여러 사용자가 같은 게시물에 동시에 별표를 주거나 클라이언트 데이터의 동기화가 어긋나도 별표가 잘못 집계되지 않습니다. 트랜잭션이 거부되면 서버에서 현재 값을 클라이언트에 반환하며, 클라이언트는 업데이트된 값으로 트랜잭션을 다시 실행합니다. 트랜잭션이 수락되거나 시도가 일정 횟수를 초과할 때까지 이 과정이 반복됩니다.


###오프라인으로 데이터 쓰기

클라이언트의 네트워크 연결이 끊겨도 앱은 계속 정상적으로 작동합니다.

Firebase 데이터베이스에 연결된 모든 클라이언트는 자체적으로 활성 데이터의 내부 버전을 유지합니다. 데이터를 쓰면 우선 로컬 버전에 기록됩니다. 그런 다음 Firebase 클라이언트가 해당 데이터를 원격 데이터베이스 서버 및 다른 클라이언트와 '최선을 다해' 동기화합니다.

이와 같이 데이터베이스에 대한 모든 쓰기 작업은 로컬 이벤트를 즉시 발생시키며, 그 이후에 서버에 데이터가 기록됩니다. 따라서 앱은 네트워크 지연 시간 또는 연결 여부에 관계없이 응답성을 유지합니다.

네트워크에 다시 연결되면 앱에서 해당 이벤트 세트를 수신하여 클라이언트와 현재 서버 상태를 동기화하므로 맞춤 코드를 별도로 작성할 필요가 없습니다.