import EmptyState from "@/components/common/EmptyState";
import ErrorAlert from "@/components/common/ErrorAlert";
import PageButton from "@/components/common/PageButton";
import PageHeader from "@/components/layout/PageHeader";
import TransactionsTable from "@/components/transactions/TransactionsTable";
import TransactionsTableSkeleton from "@/components/transactions/TransactionsTableSkeleton";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import useTransactions from "@/hooks/useTransactions";
import { parsePage } from "@/lib/pagination";
import { cn } from "cn";
import { useSearchParams } from "react-router-dom";

const PAGE_SIZE = 10;

export default function TransactionsPage() {
  const [searchParams] = useSearchParams();
  const page = parsePage(searchParams);
  const { data, isPlaceholderData, isError, isPending, refetch, isFetching } =
    useTransactions({
      size: PAGE_SIZE,
      page: page - 1,
    });
  const shownPage = data ? data.page + 1 : page;

  return (
    <div className="flex flex-col gap-8">
      <PageHeader
        title="Transaction history"
        description="All your transfers and top-ups."
      />
      <Card className="[--card-spacing:--spacing(6)]">
        <CardContent
          className={cn(
            "transition-opacity",
            isPlaceholderData && "opacity-60",
          )}
          aria-busy={isPending || isPlaceholderData}
        >
          {isPending ? (
            <TransactionsTableSkeleton rows={PAGE_SIZE} />
          ) : isError && data === undefined ? (
            <ErrorAlert
              title="Couldn't load your history"
              isRetrying={isFetching}
              onRetry={refetch}
            />
          ) : data.totalElements === 0 ? (
            <EmptyState
              title="No history yet"
              description="Your transactions will show up here."
            />
          ) : shownPage > data.totalPages ? (
            <EmptyState
              title="This page does not exist"
              description={`Your history has ${data.totalPages} ${data.totalPages === 1 ? "page" : "pages"}`}
            >
              <PageButton to={1} size="default">
                Go to page 1
              </PageButton>
            </EmptyState>
          ) : (
            <TransactionsTable transactions={data.content} />
          )}
        </CardContent>

        {data && data.totalPages > 1 && shownPage <= data.totalPages && (
          <CardFooter className="justify-between">
            <span className="caption">
              Page {shownPage} of {data.totalPages}
            </span>
            <div className="flex gap-2">
              <PageButton
                disabled={shownPage <= 1 || isPlaceholderData}
                to={page - 1}
              >
                Previous
              </PageButton>
              <PageButton
                disabled={shownPage >= data.totalPages || isPlaceholderData}
                to={page + 1}
              >
                Next
              </PageButton>
            </div>
          </CardFooter>
        )}
      </Card>
    </div>
  );
}
