import { Button } from "./ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";
import { ColumnDef } from "@tanstack/react-table";
import { MoreHorizontal } from "lucide-react";
import { TableColumnHeader } from "./TableColumnHeader";

export const createColumn = <T extends object>(
  key: keyof T,
  headerName: string
): ColumnDef<T, unknown> => ({
  accessorKey: key as string,
  header: ({ column }) => (
    <TableColumnHeader column={column} title={headerName} />
  ),
  cell: ({ row }) => {
    const value = row.original[key] as React.ReactNode;
    if (key === "action") {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const actionValue: any = row.original[key] as any;
      return (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="h-8 w-8 p-0">
              <span className="sr-only">Open menu</span>
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <div className="pl-2">
              <div className="flex gap-2 flex-col items-start">
                {actionValue && actionValue.length > 0 ? (
                  // eslint-disable-next-line @typescript-eslint/no-explicit-any
                  actionValue?.map((action: any, index: number) => (
                    <Button
                      key={index}
                      variant="ghost"
                      onClick={action.callback}
                      className="text-xs py-1 hover:bg-transparent h-auto"
                    >
                      {action.label && <span>{action.label}</span>}
                    </Button>
                  ))
                ) : (
                  <span>-</span>
                )}
              </div>
            </div>
          </DropdownMenuContent>
        </DropdownMenu>
      );
    }
    return <div>{value}</div>;
  },
});
