package com.jason.purchase_agent.service.categories;

import com.jason.purchase_agent.dto.categories.CategoryCreateDto;

import com.jason.purchase_agent.dto.categories.CategoryTreeDto;
import com.jason.purchase_agent.dto.categories.CategoryUpdateDto;
import com.jason.purchase_agent.entity.Category;
import com.jason.purchase_agent.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.function.Function;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // ---------- 카테고리 조회 ----------
    /**
     * 카테고리 트리(계층 구조)를 조회하는 메서드
     *
     * @param keyword 검색어(옵션)
     * @return 계층 구조 트리 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<CategoryTreeDto> getCategoryTree(String keyword) {
        List<Category> roots; // 최상위(루트) 카테고리 목록 저장 변수

        // 1. 검색어가 입력된 경우(키워드를 포함하는 카테고리만 보여줌)
        if (keyword != null && !keyword.trim().isEmpty()) {

            // (1) 이름/링크에 키워드를 포함하는 모든 카테고리를 조회
            // ex. filtered : 남성 건강, 여성 건강
            List<Category> filtered = categoryRepository.findAllByKeyword(keyword);

            // (2) 해당 카테고리들을 포함한 (트리형) 구조로 재조립하여 반환
            // (즉, 키워드에 맞는 노드와 그 부모까지 트리에 담기)
            return buildPrunedTrees(filtered);

        // 2. 검색어가 없는 경우(전체 카테고리 트리)
        } else {

            // (1) 루트 카테고리(부모없는 최상위 카테고리)만 조회
            roots = categoryRepository.findByParentIsNullOrderByPath();

            // (2) 각 루트에서 자식까지 재귀적으로 트리 구조 DTO로 변환
            // → roots 리스트의 각 카테고리를 toTreeDto(트리 DTO)로 만들고, 그 리스트를 결과로 반환
            return roots.stream()
                    .map(this::toTreeDto)
                    .collect(Collectors.toList());
        }
    }

    /**
     * (검색용) 필터된 카테고리와 그 조상을 포함한 트리 구조로 변환
     * - 즉, 키워드로 걸린 카테고리와 부모/조상을 살린 최소 트리를 만든다
     *
     * @param filtered 키워드에 걸린 카테고리(leaf, 중간 모두 가능)
     * @return 최소 트리형 계층 DTO 리스트
     */
    private List<CategoryTreeDto> buildPrunedTrees(List<Category> filtered) {

        // 1. 먼저 전체 루트(최상위) 카테고리를 조회한다
        // ex. roots : 보충제, 스포츠, 식료품
        List<Category> roots = categoryRepository.findByParentIsNullOrderByPath();

        // 2. 각 루트부터 재귀적으로 내려가면서,
        //    (1) 본인/자식이 filtered(검색결과)에 포함된 경우만 트리에 남김
        //    (2) 그 외는 null로 반환되어 결과 트리에서 제외됨
        return roots.stream()
                .map(toPrunedTreeDto(filtered)) // 함수형 매핑 (Category -> CategoryTreeDto, 필터링 포함)
                .filter(x -> x != null) // null(필요 없는 가지)는 제외
                .collect(Collectors.toList()); // 최종 트리 리스트로 변환해서 반환
    }

    /**
     * 특정 노드들만 남긴 가지 치기(Prune) 트리 DTO를 재귀적으로 만드는 함수(Function)
     * - keepNodes(남길 대상)가 아니면서 자식도 없는 노드는 null로 반환(트리에서 제외)
     * - 자신(match)이 keepNodes에 포함되거나, 자식 중 하나라도 남아 있으면 트리 노드 보존
     *
     * @param keepNodes 표시/유지할 카테고리 노드 목록(검색 결과 등)
     * @return Category -> CategoryTreeDto, 트리 필터링 함수
     */
    private Function <Category, CategoryTreeDto> toPrunedTreeDto(
            List<Category> keepNodes
    ){
        // 람다표현식
        // ex. cat : 보충제, 스포츠, 식료품
        return cat -> {

            // [1] 현재 루트가 keepNodes(남길 노드)에 포함되는지 체크
            // ex. 남성 건강의 id와 보충제의 id가 같으면 true, 다르면 false
            boolean match = keepNodes.stream()
                    .anyMatch(n -> n.getId().equals(cat.getId()));

            // [2] (재귀) 모든 자식(child) 노드에 대해 같은 함수 적용!
            //      - 즉, 자식 → 손자 → 증손자 ... 등으로 끝까지 깊게 따라감
            //      - 살아남는(keepNodes or 자식 잔존) 노드만 반환
            List<CategoryTreeDto> childDtos = cat.getChildren().stream()
                    .map(toPrunedTreeDto(keepNodes)) // 재귀적으로 자식방문
                    .filter(dto -> dto != null) // 살아남는 노드만
                    .collect(Collectors.toList());

            // [3] 자신이 남길 대상이거나, 자식 중 남아있는게 하나라도 있으면 이 노드를 보존
            if (match || !childDtos.isEmpty()) {
                return CategoryTreeDto.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .link(cat.getLink())
                        .path(cat.getPath())
                        .children(childDtos) // 자식(있을 때만)
                        .build();
            }

            // [4] 그렇지 않으면 본인/자식 모두 안 남기므로 트리에서 가지치기(null 리턴!)
            return null;
        };
    }

    /**
     * 하나의 카테고리(Category 엔티티 객체)를 계층형 트리 DTO로 변환하는 재귀 메소드
     * (즉, 자신+자식+자손까지 포함된 트리 구조로 만들어줌)
     */
    private CategoryTreeDto toTreeDto(Category category){
        return CategoryTreeDto.builder()
                .id(category.getId())
                .name(category.getName())
                .link(category.getLink())
                .path(category.getPath())
                // ▼ 자식 카테고리가 있는지 체크
                .children(category.getChildren() == null // 자식 목록이 null(없으면)
                        ? List.of() // 빈 리스트 반환(자식 없음)
                        // 자식 있으면 customIdx(순서)로 정렬 → 각각 toTreeDto 재귀 변환
                        : category.getChildren().stream()
                                .sorted((c1, c2) -> c1.getCustomIdx() - c2.getCustomIdx()) // customIdx 오름차순 정렬
                                .map(this::toTreeDto) // 각각의 자식도 '트리 형태(DTO)'로 변환(재귀 호출)
                                .collect(Collectors.toList())) // 리스트로 반환
                .build();
    }

    // ---------- 카테고리 생성 ----------
    /**
     * 계층형 카테고리를 생성하는 메서드
     * - 카테고리 경로 문자열(예: "전자제품>컴퓨터>노트북")을 받아서 계층 구조로 생성
     * - 중간 카테고리가 이미 존재하면 재사용하고, 없으면 새로 생성
     * - 마지막 카테고리(leaf)에만 링크 URL을 설정
     * - 트랜잭션 내에서 실행되어 중간에 실패하면 모든 변경사항이 롤백됨
     * 실행 과정:
     * 1차 루프: "전자제품" → 루트 카테고리로 생성 또는 기존 재사용
     * 2차 루프: "컴퓨터" → 전자제품의 자식으로 생성 또는 기존 재사용
     * 3차 루프: "노트북" → 컴퓨터의 자식으로 생성, 링크 설정, 중복 검증
     * 핵심 특징:
     * 중복 방지: 기존 카테고리는 재사용하여 불필요한 중복 생성 방지
     * 트랜잭션: 중간에 오류 발생시 모든 변경사항 롤백
     * 계층 path: customIdx 기반으로 "0001/0002/0003" 형태의 경로 자동 생성
     * 링크 설정: 마지막 카테고리에만 실제 상품 링크 연결
     *
     * @param dto 카테고리 생성 정보 (경로와 링크 포함)
     * @throws IllegalStateException 동일한 이름+링크의 카테고리가 이미 존재할 때
     */
    @Transactional
    public void createCategory(CategoryCreateDto dto) {

        // 1. 카테고리 경로를 ">" 구분자로 분할
        // ex : "전자제품>컴퓨터>노트북" → ["전자제품", "컴퓨터", "노트북"]
        String[] names = dto.getCategoryPath().split(" > ");

        // 2. 현재 처리 중인 카테고리의 부모 (처음엔 null = 루트 카테고리)
        Category parent = null;

        // 3. path 구성용 구분자 ("/" 문자, 첫 번째는 빈 문자열)
        String delimiter = "";

        // 4. customIdx로 만든 path 문자열을 누적하는 StringBuilder (예: "0001/0002/0003")
        StringBuilder pathBuilder = new StringBuilder();

        // 5. 분할된 카테고리 이름들을 하나씩 순서대로 처리
        for(int i=0; i<names.length; i++) {

            // 6. 현재 처리할 카테고리 이름에서 앞뒤 공백 제거
            String name = names[i].trim();

            // 7. 빈 문자열이면 건너뛰기 (잘못된 입력 처리)
            if(name.isEmpty()) continue;

            // 8. 현재 부모 하위에 같은 이름의 카테고리가 이미 존재하는지 검색
            //    parent가 null이면 루트 카테고리 중에서 검색, 아니면 특정 부모의 자식 중에서 검색
            Category exist = (parent == null)
                    ? categoryRepository.findByNameAndParentIsNull(name)
                    : categoryRepository.findByNameAndParent(name, parent);

            // === (1) 제일 마지막 노드(leaf)라면, 링크까지 같이 체크해 중복 검증 ===
            if(i == names.length-1) {
                // 9. 마지막 카테고리는 이름+링크 조합으로 중복 체크 (더 엄격한 검증)
                Category duplicated = (parent == null)
                        ? categoryRepository.findByNameAndLinkAndParentIsNull(name, dto.getLink())
                        : categoryRepository.findByParentAndNameAndLink(parent, name, dto.getLink());

                // 10. 동일한 이름+링크 조합이 이미 존재하면 예외 발생
                if(duplicated != null)
                    throw new IllegalStateException("같은 계층명과 링크의 카테고리가 이미 존재합니다!");
            }

            // 11. 이미 존재하는 카테고리가 있으면 재사용 (새로 만들지 않음)
            if(exist != null) {
                // 12. 기존 카테고리를 다음 단계의 부모로 설정
                parent = exist;

                // 13. 기존 카테고리의 customIdx를 path에 추가 (4자리 포맷: 0001, 0002...)
                pathBuilder.append(delimiter).append(String.format("%04d", exist.getCustomIdx()));

                // 14. 다음 path 구성을 위해 구분자를 "/"로 설정
                delimiter = "/";

                // 15. 기존 카테고리 재사용했으므로 다음 루프로 이동
                continue;
            }

            // === 여기서부터는 새로운 카테고리 생성 로직 ===

            // 16. 새 카테고리 엔티티 객체 생성
            Category cat = new Category();

            // 17. 카테고리 이름 설정
            cat.setName(name);

            // 18. 부모 카테고리 설정 (루트면 null, 아니면 현재 parent)
            cat.setParent(parent);

            // 19. 현재 부모 하위에서 다음 customIdx 번호 생성 (1, 2, 3... 순서)
            int customIdx = nextCustomIdx(parent != null ? parent.getId() : null);

            // 20. 생성된 customIdx 설정
            cat.setCustomIdx(customIdx);

            // 21. path 문자열에 현재 customIdx 추가 (4자리 포맷)
            pathBuilder.append(delimiter).append(String.format("%04d", customIdx));

            // 22. 다음 path 구성을 위해 구분자를 "/"로 설정
            delimiter = "/";

            // 23. 완성된 path 문자열을 카테고리에 설정
            cat.setPath(pathBuilder.toString());

            // 24. 마지막 노드(leaf)에만 링크 URL 설정
            if(i == names.length - 1) cat.setLink(dto.getLink());

            // 25. 데이터베이스에 저장하고, 저장된 엔티티를 다음 단계의 부모로 설정
            parent = categoryRepository.save(cat);
        }
    }
    // 특정 부모 하위 customIdx 최대값 + 1
    public int nextCustomIdx(String parentId) {
        Integer max = categoryRepository.findMaxCustomIdxByParent(parentId);
        return max != null ? max + 1 : 1;
    }

    /**
     * 엑셀 파일로 여러 카테고리를 일괄 등록하는 서비스 메소드
     *
     * @param excelFile MultipartFile로 받은 엑셀 파일(.xls, .xlsx)
     * @return 실제 등록 성공 건수
     */
    @Transactional
    public int uploadCategoryExcel(MultipartFile excelFile) {
        int count = 0; // 등록된 줄(행) 건수를 셀 변수
        try (
                // 1. 엑셀 파일을 읽어서 Workbook 객체(고수준 대표 객체)로 만듦
                Workbook workbook = WorkbookFactory.create(excelFile.getInputStream())
        ) {
            // 2. 첫 번째 시트(sheet)를 사용
            Sheet sheet = workbook.getSheetAt(0);

            // 3. 시트의 모든 행(row)을 순회
            for (Row row : sheet) {

                // 3-1) 첫 행(0번째)은 보통 제목이므로 그냥 넘긴다
                if (row.getRowNum() == 0) continue;

                // 3-2) 첫 번째 셀(카테고리 경로), 두 번째 셀(링크) 읽기
                Cell nameCell = row.getCell(0);
                Cell linkCell = row.getCell(1);

                // 데이터가 올바르게 없는 경우 건너뜀(null 방지)
                if (nameCell == null || linkCell == null) continue;

                // 셀 내용을 문자열로 추출
                String categoryPath = nameCell.getStringCellValue().trim();
                String link = linkCell.getStringCellValue().trim();

                // 값이 비어 있으면 등록 시도하지 않음
                if (categoryPath.isEmpty() || link.isEmpty()) continue;

                // 3-3) 실제 등록 시도 (여기서 중복 처리 포함)
                try {

                    // 등록용 DTO 객체(계층명+링크) 생성 및 값 세팅
                    CategoryCreateDto dto = new CategoryCreateDto();
                    dto.setCategoryPath(categoryPath);
                    dto.setLink(link);

                    // 기존 단일 등록 메서드 재사용(이미 중복 체크·등록 검증 포함)
                    createCategory(dto);
                    count++; // 등록 성공 건수 증가
                } catch (IllegalStateException e) {
                    // 이미 같은 데이터가 있으면 예외 발생 → 여기에선 증가X, 단순 무시(필요시 로그)
                }
            }
        } catch(Exception e) {
            // 4. 엑셀 자체 파싱/IO 오류는 IllegalStateException으로 래핑하여 호출 컨트롤러에 전달
            throw new IllegalStateException("엑셀 파일 파싱 오류: "+e.getMessage(), e);
        }
        // 5. 정상적으로 등록된 건이 하나도 없다면 예외로 안내(프론트 알림에 활용)
        if(count == 0) throw new IllegalStateException("엑셀 파일에 등록 가능한 데이터가 없습니다.");

        return count; // 성공적으로 등록된 건수를 반환
    }


















    // 카테고리 수정: name/link/parent 변경과 경로 재계산(필요 시 customIdx 재배정)
    @Transactional
    public void updateCategory(String id, CategoryUpdateDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));

        if (dto.getName() != null) category.setName(dto.getName());
        if (dto.getLink() != null) category.setLink(dto.getLink());
    }
    // 카테고리 삭제 (하위 존재 시 금지)
    @Transactional
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));

        // 하위 카테고리 존재 여부 체크 (예시: children 리스트가 비어있는지)
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new IllegalStateException("하위 카테고리가 존재하는 경우 삭제할 수 없습니다.");
        }

        categoryRepository.delete(category);
    }



}
