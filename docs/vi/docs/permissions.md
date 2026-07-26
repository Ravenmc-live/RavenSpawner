# Quyền

RavenSpawners dùng hệ thống quyền nhiều lớp. Nhấp vào node quyền để sao chép vào clipboard.

## Giá Trị Mặc Định

| Giá trị | Ý nghĩa |
|---------|---------|
| `op` | Chỉ operator máy chủ có quyền mặc định |
| `true` | Mọi người chơi có quyền mặc định |
| `false` | Không người chơi nào có quyền mặc định; phải cấp rõ ràng |

## Quyền Lệnh

::: info
Để dùng một lệnh, người chơi cần **cả** `ravenspawners.command.use` và quyền cụ thể được liệt kê dưới đây.
:::

<BaseTable :columns="['Quyền', 'Mô tả', 'Mặc định']" grid="2fr 3fr 0.6fr">

<PermRow permission="ravenspawners.command.use" defaultVal="op">Quyền gốc cần cho mọi lệnh RavenSpawners. Không có quyền này thì không truy cập được lệnh nào.</PermRow>
<PermRow permission="ravenspawners.command.reload" defaultVal="op">Cho phép tải lại cấu hình bằng <code>/ss reload</code>.</PermRow>
<PermRow permission="ravenspawners.command.give" defaultVal="op">Cho phép trao spawner bằng <code>/ss give</code>.</PermRow>
<PermRow permission="ravenspawners.command.list" defaultVal="op">Cho phép xem danh sách spawner quản trị bằng <code>/ss list</code>.</PermRow>
<PermRow permission="ravenspawners.command.hologram" defaultVal="op">Cho phép bật/tắt hologram toàn cục bằng <code>/ss hologram</code>.</PermRow>
<PermRow permission="ravenspawners.command.prices" defaultVal="op">Cho phép xem GUI giá vật phẩm bằng <code>/ss prices</code>.</PermRow>
<PermRow permission="ravenspawners.command.clear" defaultVal="op">Cho phép dọn hologram và ghost spawner bằng <code>/ss clear</code>.</PermRow>
<PermRow permission="ravenspawners.command.near" defaultVal="op">Cho phép quét và đánh dấu spawner gần đó bằng <code>/ss near</code>.</PermRow>
<PermRow permission="ravenspawners.command.set" defaultVal="op">Cho phép đặt kích thước stack, phạm vi và độ trễ bằng <code>/ss set</code>.</PermRow>
<PermRow permission="ravenspawners.command.language" defaultVal="op">Cho phép đổi ngôn ngữ bằng <code>/ss language</code>.</PermRow>
<PermRow permission="ravenspawners.command.gui_layout" defaultVal="op">Cho phép đổi bố cục GUI bằng <code>/ss gui_layout</code>.</PermRow>

</BaseTable>

## Quyền Tính Năng

<BaseTable :columns="['Quyền', 'Mô tả', 'Mặc định']" grid="2fr 3fr 0.6fr">

<PermRow permission="ravenspawners.changetype" defaultVal="op">
Cho phép đổi loại mob của spawner bằng cách nhấp phải với spawn egg.
</PermRow>

<PermRow permission="ravenspawners.stack" defaultVal="true">
Cho phép xếp chồng spawner bằng tay hoặc qua GUI stacker.
</PermRow>

<PermRow permission="ravenspawners.break" defaultVal="true">
Cho phép phá và thu thập RavenSpawners, vẫn chịu yêu cầu công cụ và Silk Touch trong cấu hình.
</PermRow>

<PermRow permission="ravenspawners.break.bypassdropchance" defaultVal="op">
Người có quyền này luôn nhận vật phẩm spawner khi phá bất kể <code>drop_chance</code>. Quyền cũng cho phép phá stack khi cúi và dùng GUI stacker đối với spawner có cấu hình tỷ lệ rơi.
</PermRow>

<PermRow permission="ravenspawners.sellall" defaultVal="true">
Cho phép bán vật phẩm từ GUI kho spawner.
</PermRow>

</BaseTable>
