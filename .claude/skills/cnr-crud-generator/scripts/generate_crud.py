#!/usr/bin/env python3
"""
CNR Domain CRUD Generator

헥사고날 아키텍처 기반 도메인 CRUD 보일러플레이트 코드를 생성합니다.

Usage:
    python generate_crud.py <DomainName> "<field1:Type[!],field2:Type[!],...>" [--base-dir <path>]

Arguments:
    DomainName   PascalCase 도메인명 (예: Product, OrderItem)
    fields       쉼표 구분 필드 정의
                   - 형식: fieldName:JavaType[!]
                   - '!' 접미사: DB NOT NULL 제약 조건
    --base-dir   프로젝트 루트 디렉터리 (기본값: 현재 디렉터리)

Examples:
    python generate_crud.py Product "name:String!,price:Long!,description:String"
    python generate_crud.py OrderItem "orderId:Long!,productId:Long!,quantity:Integer!" --base-dir /path/to/project
"""

import argparse
import os
import re
import sys
from pathlib import Path

BASE_PACKAGE = "com.toy.cnr"

# Java 타입별 필요한 import
TYPE_IMPORTS = {
    "BigDecimal": "java.math.BigDecimal",
    "LocalDate": "java.time.LocalDate",
    "LocalDateTime": "java.time.LocalDateTime",
    "LocalTime": "java.time.LocalTime",
    "UUID": "java.util.UUID",
}


# ─── 이름 변환 유틸 ──────────────────────────────────────────────────────────────

def to_camel(name: str) -> str:
    """PascalCase → camelCase"""
    return name[0].lower() + name[1:]


def to_snake(name: str) -> str:
    """PascalCase / camelCase → snake_case"""
    s = re.sub(r"(.)([A-Z][a-z]+)", r"\1_\2", name)
    return re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", s).lower()


def to_table_name(pascal: str) -> str:
    return to_snake(pascal)


# ─── 필드 파싱 ───────────────────────────────────────────────────────────────────

def parse_fields(raw: str) -> list[dict]:
    fields = []
    for token in raw.split(","):
        token = token.strip()
        if ":" not in token:
            sys.exit(f"❌ 잘못된 필드 정의: '{token}'  형식: fieldName:Type[!]")
        fname, tdef = token.split(":", 1)
        fname = fname.strip()
        tdef = tdef.strip()
        not_null = tdef.endswith("!")
        java_type = tdef.rstrip("!")
        fields.append({
            "name": fname,
            "type": java_type,
            "not_null": not_null,
            "col": to_snake(fname),
        })
    return fields


def extra_imports(fields: list[dict]) -> str:
    seen = set()
    lines = []
    for f in fields:
        imp = TYPE_IMPORTS.get(f["type"])
        if imp and imp not in seen:
            seen.add(imp)
            lines.append(f"import {imp};")
    return ("\n".join(lines) + "\n") if lines else ""


def test_json_value(java_type: str, create: bool = True) -> str:
    """타입별 테스트용 JSON 값 (Java text block 내에 삽입)"""
    if java_type == "String":
        return '"test-value"' if create else '"updated-value"'
    elif java_type == "Long":
        return "100" if create else "200"
    elif java_type == "Integer":
        return "1" if create else "2"
    elif java_type == "Double":
        return "1.5" if create else "2.5"
    elif java_type == "Boolean":
        return "true"
    elif java_type == "BigDecimal":
        return "10.00" if create else "20.00"
    elif java_type == "LocalDate":
        return '"2026-01-01"'
    elif java_type == "LocalDateTime":
        return '"2026-01-01T10:00:00"' if create else '"2026-06-01T10:00:00"'
    elif java_type == "LocalTime":
        return '"10:00:00"'
    elif java_type == "UUID":
        return '"550e8400-e29b-41d4-a716-446655440000"' if create else '"6ba7b810-9dad-11d1-80b4-00c04fd430c8"'
    return '"test-value"' if create else '"updated-value"'


def java_assert_value(java_type: str, create: bool = True):
    """MockMvc jsonPath().value() 인수. None 이면 exists() 만 체크."""
    if java_type == "String":
        return '"test-value"' if create else '"updated-value"'
    elif java_type == "Long":
        return "100" if create else "200"
    elif java_type == "Integer":
        return "1" if create else "2"
    elif java_type == "Double":
        return "1.5" if create else "2.5"
    elif java_type == "Boolean":
        return "true"
    else:
        return None  # BigDecimal, LocalDate, LocalDateTime, LocalTime, UUID → exists() 만


# ─── 코드 템플릿 ─────────────────────────────────────────────────────────────────

def tpl_domain(D: str, cc: str, fields: list[dict]) -> str:
    pkg = f"{BASE_PACKAGE}.domain.{cc}"
    imps = extra_imports(fields)
    imp_block = ("\n" + imps) if imps else ""
    params = "\n".join(f"    {f['type']} {f['name']}," for f in fields).rstrip(",")
    return f"""\
package {pkg};
{imp_block}
public record {D}(
    Long id,
{params}
) {{
}}
"""


def tpl_create_command(D: str, cc: str, fields: list[dict]) -> str:
    pkg = f"{BASE_PACKAGE}.domain.{cc}"
    imps = extra_imports(fields)
    imp_block = ("\n" + imps) if imps else ""
    params = "\n".join(f"    {f['type']} {f['name']}," for f in fields).rstrip(",")
    return f"""\
package {pkg};
{imp_block}
public record {D}CreateCommand(
{params}
) {{
}}
"""


def tpl_update_command(D: str, cc: str, fields: list[dict]) -> str:
    pkg = f"{BASE_PACKAGE}.domain.{cc}"
    imps = extra_imports(fields)
    imp_block = ("\n" + imps) if imps else ""
    params = "\n".join(f"    {f['type']} {f['name']}," for f in fields).rstrip(",")
    return f"""\
package {pkg};
{imp_block}
public record {D}UpdateCommand(
{params}
) {{
}}
"""


def tpl_dto(D: str, cc: str, fields: list[dict]) -> str:
    pkg = f"{BASE_PACKAGE}.port.{cc}.model"
    imps = extra_imports(fields)
    imp_block = ("\n" + imps) if imps else ""
    params = "\n".join(f"    {f['type']} {f['name']}," for f in fields).rstrip(",")
    return f"""\
package {pkg};
{imp_block}
public record {D}Dto(
    Long id,
{params}
) {{
}}
"""


def tpl_create_dto(D: str, cc: str, fields: list[dict]) -> str:
    pkg = f"{BASE_PACKAGE}.port.{cc}.model"
    imps = extra_imports(fields)
    imp_block = ("\n" + imps) if imps else ""
    params = "\n".join(f"    {f['type']} {f['name']}," for f in fields).rstrip(",")
    return f"""\
package {pkg};
{imp_block}
public record {D}CreateDto(
{params}
) {{
}}
"""


def tpl_update_dto(D: str, cc: str, fields: list[dict]) -> str:
    pkg = f"{BASE_PACKAGE}.port.{cc}.model"
    imps = extra_imports(fields)
    imp_block = ("\n" + imps) if imps else ""
    params = "\n".join(f"    {f['type']} {f['name']}," for f in fields).rstrip(",")
    return f"""\
package {pkg};
{imp_block}
public record {D}UpdateDto(
{params}
) {{
}}
"""


def tpl_repository(D: str, cc: str) -> str:
    pkg = f"{BASE_PACKAGE}.port.{cc}"
    return f"""\
package {pkg};

import {BASE_PACKAGE}.port.common.RepositoryResult;
import {pkg}.model.{D}CreateDto;
import {pkg}.model.{D}Dto;
import {pkg}.model.{D}UpdateDto;

import java.util.List;

public interface {D}Repository {{
    RepositoryResult<List<{D}Dto>> findAll();
    RepositoryResult<{D}Dto> findById(Long id);
    RepositoryResult<{D}Dto> save({D}CreateDto dto);
    RepositoryResult<{D}Dto> update(Long id, {D}UpdateDto dto);
    RepositoryResult<Void> deleteById(Long id);
}}
"""


def tpl_mapper(D: str, cc: str, fields: list[dict]) -> str:
    pkg = f"{BASE_PACKAGE}.application.{cc}.mapper"
    domain_pkg = f"{BASE_PACKAGE}.domain.{cc}"
    port_pkg = f"{BASE_PACKAGE}.port.{cc}.model"
    imps = extra_imports(fields)

    field_args = ", ".join(f"dto.{f['name']}()" for f in fields)
    create_args = ", ".join(f"command.{f['name']}()" for f in fields)
    update_args = ", ".join(f"command.{f['name']}()" for f in fields)

    return f"""\
package {pkg};

import {domain_pkg}.{D};
import {domain_pkg}.{D}CreateCommand;
import {domain_pkg}.{D}UpdateCommand;
import {port_pkg}.{D}CreateDto;
import {port_pkg}.{D}Dto;
import {port_pkg}.{D}UpdateDto;
{imps}import lombok.experimental.UtilityClass;

@UtilityClass
public class {D}Mapper {{

    public static {D} toDomain({D}Dto dto) {{
        return new {D}(
            dto.id(),
            {field_args}
        );
    }}

    public static {D}CreateDto toExternal({D}CreateCommand command) {{
        return new {D}CreateDto(
            {create_args}
        );
    }}

    public static {D}UpdateDto toExternal({D}UpdateCommand command) {{
        return new {D}UpdateDto(
            {update_args}
        );
    }}
}}
"""


def tpl_service(D: str, cc: str) -> str:
    pkg = f"{BASE_PACKAGE}.application.{cc}.service"
    return f"""\
package {pkg};

import {BASE_PACKAGE}.application.common.ResultMapper;
import {BASE_PACKAGE}.application.{cc}.mapper.{D}Mapper;
import {BASE_PACKAGE}.domain.common.CommandResult;
import {BASE_PACKAGE}.domain.{cc}.{D};
import {BASE_PACKAGE}.domain.{cc}.{D}CreateCommand;
import {BASE_PACKAGE}.domain.{cc}.{D}UpdateCommand;
import {BASE_PACKAGE}.port.{cc}.{D}Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class {D}QueryService {{

    private final {D}Repository {to_camel(D)}Repository;

    public {D}QueryService({D}Repository {to_camel(D)}Repository) {{
        this.{to_camel(D)}Repository = {to_camel(D)}Repository;
    }}

    public CommandResult<List<{D}>> findAll() {{
        return ResultMapper.toCommandResult(
            {to_camel(D)}Repository.findAll()
                .map(list -> list.stream().map({D}Mapper::toDomain).toList())
        );
    }}

    public CommandResult<{D}> findById(Long id) {{
        return ResultMapper.toCommandResult({to_camel(D)}Repository.findById(id))
            .map({D}Mapper::toDomain);
    }}

    public CommandResult<{D}> create({D}CreateCommand command) {{
        return ResultMapper.toCommandResult({to_camel(D)}Repository.save({D}Mapper.toExternal(command)))
            .map({D}Mapper::toDomain);
    }}

    public CommandResult<{D}> update(Long id, {D}UpdateCommand command) {{
        return ResultMapper.toCommandResult({to_camel(D)}Repository.update(id, {D}Mapper.toExternal(command)))
            .map({D}Mapper::toDomain);
    }}

    public CommandResult<Void> delete(Long id) {{
        return ResultMapper.toCommandResult({to_camel(D)}Repository.deleteById(id));
    }}
}}
"""


def tpl_api(D: str, cc: str) -> str:
    pkg = f"{BASE_PACKAGE}.api.{cc}"
    url_path = to_snake(D).replace("_", "-")
    return f"""\
package {pkg};

import {BASE_PACKAGE}.api.common.util.ResponseMapper;
import {pkg}.request.{D}CreateRequest;
import {pkg}.request.{D}UpdateRequest;
import {pkg}.response.{D}Response;
import {pkg}.usecase.{D}UseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "{D}", description = "{D} 도메인 CRUD API")
@RestController
@RequestMapping("/v1/{url_path}")
public class {D}Api {{

    private final {D}UseCase {to_camel(D)}UseCase;

    public {D}Api({D}UseCase {to_camel(D)}UseCase) {{
        this.{to_camel(D)}UseCase = {to_camel(D)}UseCase;
    }}

    @Operation(summary = "전체 {D} 조회")
    @GetMapping
    public ResponseEntity<List<{D}Response>> findAll() {{
        return ResponseMapper.toResponseEntity({to_camel(D)}UseCase.findAll());
    }}

    @Operation(summary = "{D} 단건 조회")
    @GetMapping("/{{id}}")
    public ResponseEntity<{D}Response> findById(
        @Parameter(description = "{D} ID") @PathVariable Long id
    ) {{
        return ResponseMapper.toResponseEntity({to_camel(D)}UseCase.findById(id));
    }}

    @Operation(summary = "{D} 생성")
    @PostMapping
    public ResponseEntity<{D}Response> create(@RequestBody {D}CreateRequest request) {{
        return ResponseMapper.toResponseEntity({to_camel(D)}UseCase.create(request));
    }}

    @Operation(summary = "{D} 수정")
    @PutMapping("/{{id}}")
    public ResponseEntity<{D}Response> update(
        @Parameter(description = "{D} ID") @PathVariable Long id,
        @RequestBody {D}UpdateRequest request
    ) {{
        return ResponseMapper.toResponseEntity({to_camel(D)}UseCase.update(id, request));
    }}

    @Operation(summary = "{D} 삭제")
    @DeleteMapping("/{{id}}")
    public ResponseEntity<Void> delete(
        @Parameter(description = "{D} ID") @PathVariable Long id
    ) {{
        return ResponseMapper.toNoContentResponse({to_camel(D)}UseCase.delete(id));
    }}
}}
"""


def tpl_usecase(D: str, cc: str) -> str:
    pkg = f"{BASE_PACKAGE}.api.{cc}.usecase"
    return f"""\
package {pkg};

import {BASE_PACKAGE}.api.{cc}.request.{D}CreateRequest;
import {BASE_PACKAGE}.api.{cc}.request.{D}UpdateRequest;
import {BASE_PACKAGE}.api.{cc}.response.{D}Response;
import {BASE_PACKAGE}.application.{cc}.service.{D}QueryService;
import {BASE_PACKAGE}.domain.common.CommandResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class {D}UseCase {{

    private final {D}QueryService {to_camel(D)}QueryService;

    public {D}UseCase({D}QueryService {to_camel(D)}QueryService) {{
        this.{to_camel(D)}QueryService = {to_camel(D)}QueryService;
    }}

    public CommandResult<List<{D}Response>> findAll() {{
        return {to_camel(D)}QueryService.findAll()
            .map(list -> list.stream().map({D}Response::from).toList());
    }}

    public CommandResult<{D}Response> findById(Long id) {{
        return {to_camel(D)}QueryService.findById(id)
            .map({D}Response::from);
    }}

    public CommandResult<{D}Response> create({D}CreateRequest request) {{
        return {to_camel(D)}QueryService.create(request.toCommand())
            .map({D}Response::from);
    }}

    public CommandResult<{D}Response> update(Long id, {D}UpdateRequest request) {{
        return {to_camel(D)}QueryService.update(id, request.toCommand())
            .map({D}Response::from);
    }}

    public CommandResult<Void> delete(Long id) {{
        return {to_camel(D)}QueryService.delete(id);
    }}
}}
"""


def tpl_create_request(D: str, cc: str, fields: list[dict]) -> str:
    pkg = f"{BASE_PACKAGE}.api.{cc}.request"
    imps = extra_imports(fields)
    schema_fields = ",\n\n".join(
        f"    @Schema(description = \"{f['name']}\")\n    {f['type']} {f['name']}"
        for f in fields
    )
    cmd_args = ", ".join(f"this.{f['name']}" for f in fields)
    return f"""\
package {pkg};

import {BASE_PACKAGE}.domain.{cc}.{D}CreateCommand;
{imps}import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "{D} 생성 요청")
public record {D}CreateRequest(
{schema_fields}
) {{
    public {D}CreateCommand toCommand() {{
        return new {D}CreateCommand({cmd_args});
    }}
}}
"""


def tpl_update_request(D: str, cc: str, fields: list[dict]) -> str:
    pkg = f"{BASE_PACKAGE}.api.{cc}.request"
    imps = extra_imports(fields)
    schema_fields = ",\n\n".join(
        f"    @Schema(description = \"{f['name']}\")\n    {f['type']} {f['name']}"
        for f in fields
    )
    cmd_args = ", ".join(f"this.{f['name']}" for f in fields)
    return f"""\
package {pkg};

import {BASE_PACKAGE}.domain.{cc}.{D}UpdateCommand;
{imps}import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "{D} 수정 요청")
public record {D}UpdateRequest(
{schema_fields}
) {{
    public {D}UpdateCommand toCommand() {{
        return new {D}UpdateCommand({cmd_args});
    }}
}}
"""


def tpl_response(D: str, cc: str, fields: list[dict]) -> str:
    pkg = f"{BASE_PACKAGE}.api.{cc}.response"
    imps = extra_imports(fields)
    schema_fields = ",\n\n".join(
        f"    @Schema(description = \"{f['name']}\")\n    {f['type']} {f['name']}"
        for f in fields
    )
    from_args = "\n".join(f"            {to_camel(D)}.{f['name']}()," for f in fields).rstrip(",")
    return f"""\
package {pkg};

import {BASE_PACKAGE}.domain.{cc}.{D};
{imps}import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "{D} 응답")
public record {D}Response(
    @Schema(description = "{D} ID")
    Long id,

{schema_fields}
) {{
    public static {D}Response from({D} {to_camel(D)}) {{
        return new {D}Response(
            {to_camel(D)}.id(),
{from_args}
        );
    }}
}}
"""


def tpl_entity(D: str, cc: str, fields: list[dict]) -> str:
    pkg = f"{BASE_PACKAGE}.rds.{cc}.entity"
    port_pkg = f"{BASE_PACKAGE}.port.{cc}.model"
    table = to_table_name(D)
    imps = extra_imports(fields)

    # 필드 선언
    field_decls = []
    for f in fields:
        if f["not_null"]:
            field_decls.append(f'    @Column(name = "{f["col"]}", nullable = false)\n    private {f["type"]} {f["name"]};')
        else:
            field_decls.append(f'    @Column(name = "{f["col"]}")\n    private {f["type"]} {f["name"]};')
    fields_block = "\n\n".join(field_decls)

    # 생성자 파라미터
    ctor_params = ", ".join(f"{f['type']} {f['name']}" for f in fields)
    ctor_assigns = "\n".join(f"        this.{f['name']} = {f['name']};" for f in fields)

    # create 팩토리
    create_args = ", ".join(f"from.{f['name']}()" for f in fields)

    # update
    update_assigns = "\n".join(f"        this.{f['name']} = from.{f['name']}();" for f in fields)

    # toDto
    dto_args = ", ".join(f"{f['name']}" for f in fields)

    return f"""\
package {pkg};

import {port_pkg}.{D}CreateDto;
import {port_pkg}.{D}Dto;
import {port_pkg}.{D}UpdateDto;
{imps}import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "{table}")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class {D}Entity {{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

{fields_block}

    private {D}Entity(Long id, {ctor_params}) {{
        this.id = id;
{ctor_assigns}
    }}

    public static {D}Entity create({D}CreateDto from) {{
        return new {D}Entity(null, {create_args});
    }}

    public void update({D}UpdateDto from) {{
{update_assigns}
    }}

    public {D}Dto toDto() {{
        return new {D}Dto(id, {dto_args});
    }}
}}
"""


def tpl_jpa_repository(D: str, cc: str) -> str:
    pkg = f"{BASE_PACKAGE}.rds.{cc}"
    return f"""\
package {pkg};

import {pkg}.entity.{D}Entity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface {D}JpaRepository extends JpaRepository<{D}Entity, Long> {{
}}
"""


def tpl_repository_impl(D: str, cc: str) -> str:
    pkg = f"{BASE_PACKAGE}.rds.{cc}"
    port_pkg = f"{BASE_PACKAGE}.port.{cc}"
    return f"""\
package {pkg};

import {BASE_PACKAGE}.port.common.RepositoryResult;
import {port_pkg}.{D}Repository;
import {port_pkg}.model.{D}CreateDto;
import {port_pkg}.model.{D}Dto;
import {port_pkg}.model.{D}UpdateDto;
import {pkg}.entity.{D}Entity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class {D}RepositoryImpl implements {D}Repository {{

    private final {D}JpaRepository {to_camel(D)}JpaRepository;

    public {D}RepositoryImpl({D}JpaRepository {to_camel(D)}JpaRepository) {{
        this.{to_camel(D)}JpaRepository = {to_camel(D)}JpaRepository;
    }}

    @Override
    public RepositoryResult<List<{D}Dto>> findAll() {{
        return RepositoryResult.wrap(() -> {{
            var list = {to_camel(D)}JpaRepository.findAll().stream()
                .map({D}Entity::toDto)
                .toList();
            return new RepositoryResult.Found<>(list);
        }});
    }}

    @Override
    public RepositoryResult<{D}Dto> findById(Long id) {{
        return RepositoryResult.ofOptional(
            () -> {to_camel(D)}JpaRepository.findById(id).map({D}Entity::toDto),
            "{D} not found with id: " + id
        );
    }}

    @Override
    public RepositoryResult<{D}Dto> save({D}CreateDto dto) {{
        return RepositoryResult.wrap(() -> {{
            var saved = {to_camel(D)}JpaRepository.save({D}Entity.create(dto));
            return new RepositoryResult.Found<>(saved.toDto());
        }});
    }}

    @Override
    public RepositoryResult<{D}Dto> update(Long id, {D}UpdateDto dto) {{
        return RepositoryResult.ofOptional(
            () -> {to_camel(D)}JpaRepository.findById(id).map(entity -> {{
                entity.update(dto);
                return {to_camel(D)}JpaRepository.save(entity).toDto();
            }}),
            "{D} not found with id: " + id
        );
    }}

    @Override
    public RepositoryResult<Void> deleteById(Long id) {{
        return RepositoryResult.wrap(() -> {{
            {to_camel(D)}JpaRepository.deleteById(id);
            return new RepositoryResult.Found<>(null);
        }});
    }}
}}
"""


# ─── 통합 테스트 템플릿 ───────────────────────────────────────────────────────────

def tpl_abstract_integration_test() -> str:
    return f'''\
package {BASE_PACKAGE};

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public abstract class AbstractIntegrationTest {{

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:17");

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redis =
        new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {{
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }}
}}
'''


def tpl_integration_test(D: str, cc: str, fields: list[dict]) -> str:
    pkg = f"{BASE_PACKAGE}.api.{cc}"
    url_path = to_snake(D).replace("_", "-")

    def make_json_lines(create: bool) -> str:
        lines = []
        for i, f in enumerate(fields):
            val = test_json_value(f["type"], create)
            comma = "," if i < len(fields) - 1 else ""
            lines.append(f'                    "{f["name"]}": {val}{comma}')
        return "\n".join(lines)

    create_fields = make_json_lines(True)
    update_fields = make_json_lines(False)

    def make_assertions(create: bool) -> str:
        lines = []
        for f in fields:
            val = java_assert_value(f["type"], create)
            if val is not None:
                lines.append(f'            .andExpect(jsonPath("$.{f["name"]}").value({val}))')
            else:
                lines.append(f'            .andExpect(jsonPath("$.{f["name"]}").exists())')
        return "\n".join(lines)

    create_assertions = make_assertions(True)
    update_assertions = make_assertions(False)

    return f'''\
package {pkg};

import com.fasterxml.jackson.databind.ObjectMapper;
import {BASE_PACKAGE}.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class {D}ApiIntegrationTest extends AbstractIntegrationTest {{

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private static final String BASE_URL = "/v1/{url_path}";

    // ── 샘플 요청 JSON ──────────────────────────────────────────────────────────

    private String createRequest() {{
        return """
                {{
{create_fields}
                }}
                """;
    }}

    private String updateRequest() {{
        return """
                {{
{update_fields}
                }}
                """;
    }}

    // ── 헬퍼 ────────────────────────────────────────────────────────────────────

    private long createSample() throws Exception {{
        var response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }}

    // ── 테스트 케이스 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll: 데이터 없을 때 200 빈 배열 반환")
    void findAll_empty_returns200WithEmptyList() throws Exception {{
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }}

    @Test
    @DisplayName("findAll: 데이터 있을 때 200 배열 반환")
    void findAll_withData_returns200WithItems() throws Exception {{
        createSample();
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
    }}

    @Test
    @DisplayName("findById: 존재하는 id 조회 시 200 반환")
    void findById_existingId_returns200() throws Exception {{
        long id = createSample();
        mockMvc.perform(get(BASE_URL + "/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));
    }}

    @Test
    @DisplayName("findById: 존재하지 않는 id 조회 시 404 반환")
    void findById_nonExistingId_returns404() throws Exception {{
        mockMvc.perform(get(BASE_URL + "/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorType").value("NOT_FOUND"));
    }}

    @Test
    @DisplayName("create: 유효한 요청 시 200 및 생성된 데이터 반환")
    void create_validRequest_returns200WithCreatedData() throws Exception {{
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
{create_assertions};
    }}

    @Test
    @DisplayName("update: 존재하는 id 수정 시 200 및 수정된 데이터 반환")
    void update_existingId_returns200WithUpdatedData() throws Exception {{
        long id = createSample();

        mockMvc.perform(put(BASE_URL + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
{update_assertions};
    }}

    @Test
    @DisplayName("update: 존재하지 않는 id 수정 시 404 반환")
    void update_nonExistingId_returns404() throws Exception {{
        mockMvc.perform(put(BASE_URL + "/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorType").value("NOT_FOUND"));
    }}

    @Test
    @DisplayName("delete: 존재하는 id 삭제 시 204 반환")
    void delete_existingId_returns204() throws Exception {{
        long id = createSample();
        mockMvc.perform(delete(BASE_URL + "/" + id))
            .andExpect(status().isNoContent());
    }}

    @Test
    @DisplayName("delete: 존재하지 않는 id 삭제 시 204 반환 (멱등성)")
    void delete_nonExistingId_returns204() throws Exception {{
        mockMvc.perform(delete(BASE_URL + "/999999"))
            .andExpect(status().isNoContent());
    }}
}}
'''


# ─── 파일 생성 ───────────────────────────────────────────────────────────────────

def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")
    print(f"  ✅  {path}")


def src(base: Path, module: str) -> Path:
    return base / module / "src" / "main" / "java"


def update_bootstrap_build_gradle(base_dir: Path) -> None:
    """module-bootstrap/build.gradle에 testcontainers 의존성 추가 (없을 때만)"""
    gradle_path = base_dir / "module-bootstrap" / "build.gradle"
    if not gradle_path.exists():
        print(f"  ⚠️  {gradle_path} 없음 — testcontainers 의존성을 수동으로 추가하세요")
        return
    content = gradle_path.read_text(encoding="utf-8")
    if "testcontainers" in content:
        return
    new_deps = (
        "    testImplementation 'org.springframework.boot:spring-boot-testcontainers'\n"
        "    testImplementation 'org.testcontainers:postgresql'\n"
        "    testImplementation 'org.testcontainers:junit-jupiter'\n"
        "    testImplementation 'org.springframework:spring-tx'"
    )
    anchor = "    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'"
    if anchor in content:
        content = content.replace(anchor, anchor + "\n" + new_deps)
        gradle_path.write_text(content, encoding="utf-8")
        print(f"  ✅  {gradle_path} (testcontainers 의존성 추가)")
    else:
        print(f"  ⚠️  {gradle_path} — testcontainers 의존성을 수동으로 추가하세요")


def generate(domain: str, fields: list[dict], base_dir: Path) -> None:
    D = domain
    cc = to_camel(D).lower()  # 패키지명: 소문자 (e.g. product, orderitem)
    pkg_root = BASE_PACKAGE.replace(".", "/")

    print(f"\n🔨  {D} 도메인 CRUD 생성 중 (base: {base_dir})\n")

    domain_src = src(base_dir, "module-core/domain") / pkg_root / "domain" / cc
    port_src = src(base_dir, "module-core/port") / pkg_root / "port" / cc
    app_src = src(base_dir, "module-core/application") / pkg_root / "application" / cc
    api_src = src(base_dir, "module-adaptor/inbound/api") / pkg_root / "api" / cc
    rds_src = src(base_dir, "module-adaptor/outbound/rds") / pkg_root / "rds" / cc

    files = [
        # domain
        (domain_src / f"{D}.java",                         tpl_domain(D, cc, fields)),
        (domain_src / f"{D}CreateCommand.java",            tpl_create_command(D, cc, fields)),
        (domain_src / f"{D}UpdateCommand.java",            tpl_update_command(D, cc, fields)),
        # port
        (port_src / f"{D}Repository.java",                 tpl_repository(D, cc)),
        (port_src / "model" / f"{D}Dto.java",              tpl_dto(D, cc, fields)),
        (port_src / "model" / f"{D}CreateDto.java",        tpl_create_dto(D, cc, fields)),
        (port_src / "model" / f"{D}UpdateDto.java",        tpl_update_dto(D, cc, fields)),
        # application
        (app_src / "mapper" / f"{D}Mapper.java",           tpl_mapper(D, cc, fields)),
        (app_src / "service" / f"{D}QueryService.java",    tpl_service(D, cc)),
        # api
        (api_src / f"{D}Api.java",                         tpl_api(D, cc)),
        (api_src / "usecase" / f"{D}UseCase.java",         tpl_usecase(D, cc)),
        (api_src / "request" / f"{D}CreateRequest.java",   tpl_create_request(D, cc, fields)),
        (api_src / "request" / f"{D}UpdateRequest.java",   tpl_update_request(D, cc, fields)),
        (api_src / "response" / f"{D}Response.java",       tpl_response(D, cc, fields)),
        # rds
        (rds_src / "entity" / f"{D}Entity.java",           tpl_entity(D, cc, fields)),
        (rds_src / f"{D}JpaRepository.java",               tpl_jpa_repository(D, cc)),
        (rds_src / f"{D}RepositoryImpl.java",              tpl_repository_impl(D, cc)),
    ]

    for path, content in files:
        write(path, content)

    # ── 통합 테스트 ────────────────────────────────────────────────────────────────
    update_bootstrap_build_gradle(base_dir)

    test_base = base_dir / "module-bootstrap" / "src" / "test" / "java" / pkg_root
    abstract_path = test_base / "AbstractIntegrationTest.java"
    abstract_is_new = not abstract_path.exists()
    if abstract_is_new:
        write(abstract_path, tpl_abstract_integration_test())

    integration_test_path = test_base / "api" / cc / f"{D}ApiIntegrationTest.java"
    write(integration_test_path, tpl_integration_test(D, cc, fields))

    test_file_count = 1 + (1 if abstract_is_new else 0)
    print(f"\n✅  {len(files) + test_file_count}개 파일 생성 완료! (테스트 {test_file_count}개 포함)")
    print(f"\n📋  생성된 파일 요약:")
    print(f"    Domain    : module-core/domain  → {D}, {D}CreateCommand, {D}UpdateCommand")
    print(f"    Port      : module-core/port    → {D}Repository, {D}Dto, {D}CreateDto, {D}UpdateDto")
    print(f"    App       : module-core/app     → {D}Mapper, {D}QueryService")
    print(f"    API       : inbound/api         → {D}Api, {D}UseCase, {D}CreateRequest, {D}UpdateRequest, {D}Response")
    print(f"    RDS       : outbound/rds        → {D}Entity, {D}JpaRepository, {D}RepositoryImpl")
    if abstract_is_new:
        print(f"    Test      : module-bootstrap/test → AbstractIntegrationTest, {D}ApiIntegrationTest (9 cases)")
    else:
        print(f"    Test      : module-bootstrap/test → {D}ApiIntegrationTest (9 cases)")
    print(f"\n🗄️   테이블명: {to_table_name(D)}")


# ─── CLI 진입점 ──────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="CNR 헥사고날 아키텍처 도메인 CRUD 코드 생성기",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument("domain", help="PascalCase 도메인명 (예: Product, OrderItem)")
    parser.add_argument("fields", help="필드 정의 (예: \"name:String!,price:Long!,description:String\")")
    parser.add_argument("--base-dir", default=".", help="프로젝트 루트 디렉터리 (기본값: 현재 디렉터리)")
    args = parser.parse_args()

    # 유효성 검사
    if not re.match(r"^[A-Z][a-zA-Z0-9]*$", args.domain):
        sys.exit(f"❌ 도메인명은 PascalCase여야 합니다 (예: Product, OrderItem). 입력값: '{args.domain}'")

    base_dir = Path(args.base_dir).resolve()
    if not base_dir.exists():
        sys.exit(f"❌ 프로젝트 루트 디렉터리가 존재하지 않습니다: {base_dir}")

    fields = parse_fields(args.fields)
    generate(args.domain, fields, base_dir)


if __name__ == "__main__":
    main()
