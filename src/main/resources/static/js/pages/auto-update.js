// 마진율, 쿠폰할인율, 최소마진, 총구매가격 입력시 예상 판매가 실시간 표시

$(document).ready(function(){

  // 폼 제출 이벤트
  $("#batch-update-form").submit(function(e){
    e.preventDefault();
    // Ajax로 supplier, marginRate 넘겨서 서버에서 비동기로 배치 등록(rabbitMQ 메시지 생성)
    $.post('/auto-update/run', {
      supplierCode: $("#supplierSelect").val(),
      marginRate: $("#marginInput").val(),
      couponRate: $("#couponInput").val(),
      minMarginPrice: $("#minMarginInput").val(),
    }, function(resp){
      alert("업데이트 작업이 시작되었습니다."); // 성공 안내
    });
  });

  // 공급업체 셀렉트박스 옵션 동적 로딩
  /*$.get('/auto-update/suppliers', function(suppliers) {
    const $sel = $("#supplierSelect");
    $sel.empty(); // 혹시 이전 option 남아있으면 제거
    suppliers.forEach(s => {
      // Supplier 엔티티에 supplierCode, supplierName이 있다고 가정
      // <option value="IHB" text="아이허브" selected>
      $sel.append($('<option>', {
        value: s.supplierCode,
        text: s.supplierName,
        selected: (s.supplierName && s.supplierName.toLowerCase().includes("iherb"))
      }));
    });
    // 만약 Iherb가 없다면 첫 번째 항목을 선택
    if ($sel.val() == null && $sel.children().length > 0) {
      $sel.val($sel.children().first().val());
    }
  });*/
});